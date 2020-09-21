/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.toscana.rete.lamma.prototype.layers;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import dk.dma.epd.common.prototype.layers.wms.AsyncWMSService;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;


import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.*;
// https://geoportale.lamma.rete.toscana.it/geoserver_meteo/wms?service=WMS&request=GetMap&layers=meteo%3Aswh&styles=&format=image%2Fpng&transparent=true&version=1.3.0&tiled=true&srs=EPSG%3A3857&time=2020-06-26T15%3A00%3A00.000Z&width=256&height=256&crs=EPSG%3A3857&bbox=782715.1696402049,5322463.153553395,939258.203568246,5479006.187481435
public class StreamingTiledWmsTimeService extends TiledWMSTimeService implements
        Runnable, AsyncWMSService {

    private boolean shouldRun = true;
    ConcurrentHashMap<String, OMGraphicList> cache = new ConcurrentHashMap<String, OMGraphicList>();
    ConcurrentHashMap<String, OMGraphicList> tmpCache = new ConcurrentHashMap<String, OMGraphicList>();
    LinkedBlockingDeque<Projection> projectionJobs = new LinkedBlockingDeque<>(
            1);

    private Thread t;
    private ExecutorService handOffPool = Executors.newFixedThreadPool(1);

    public StreamingTiledWmsTimeService(String wmsQuery, int tileNumber) {
        super(wmsQuery, tileNumber);

        this.t = new Thread(this);
        this.t.start();
    }

    public StreamingTiledWmsTimeService(String wmsQuery, int tileNumber, ConcurrentHashMap<String, OMGraphicList> sharedCache) {
        super(wmsQuery, tileNumber);
        this.cache = sharedCache;
        
        this.t = new Thread(this);
        this.t.start();
    }

    @Override
    public OMGraphicList getWmsList(Projection p) {
        final OMGraphicList result = new OMGraphicList();
        final String key = getID(p);
        if (cache.containsKey(key)) {
            // LOG.debug("CACHE HIT");
            tmpCache.remove(key);
            result.addAll(cache.get(key));
        } else if (tmpCache.containsKey(key)) {
            // LOG.debug("TMPCACHE HIT");
            result.addAll(tmpCache.get(key));
        }

        return result;
    }

    /**
     * Stop the thread
     */
    public void stop() {
        shouldRun = false;
    }

    @Override
    public void run() {
        while (shouldRun) {
            LOG.info("Running StreamingTiledWmsService");
            Projection job = null;
            try {
                // blocks until projection bbox job ready
                job = projectionJobs.takeLast();

                Thread.sleep(1000); // only take jobs every second

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (cache.containsKey(getID(job))) {
                clearCache(); // Perchè la pulisce se la contiene? questo non è logico
            } else {
                LOG.debug("JOB TAKEN");
                asyncDownload(job);
                
            }
        }
    }

    private void clearCache() {
        // Drop first 30 elements and not all
        if (cache.keySet().size() > 128) {
            LOG.debug("pruning cache (memory concern)");
            Enumeration e = cache.keys();
            for(int i =0; i < 30 && e.hasMoreElements(); i++) {
                String key = (String) e.nextElement();
                cache.remove(e);
                tmpCache.remove(e);
            }

        }
    }

    public void clearAllCache() {
        cache.clear();
        tmpCache.clear();
    }

    /**
     * Complete asynchronous download and update of caches
     * 
     * @param job
     */
    public void asyncDownload(final Projection job) {
        handOffPool.execute(new Runnable() {

            @Override
            public void run() {
                Collection<SingleWMSTimeService> workers = getTiles(job);
                OMGraphicList result = new OMGraphicList();

                ExecutorService pool = Executors.newFixedThreadPool(Math.max(workers.size(),4));
                ExecutorCompletionService<OMGraphicList> completionService = new ExecutorCompletionService<>(
                        pool);

                for (SingleWMSTimeService w : workers) {
                    completionService.submit(w);
                }

                boolean allSuccess = true;
                int legendOffset = 0;
                for (int i = 0; i < workers.size(); i++) {
                    Future<OMGraphicList> future;
                    try {
                        future = completionService.poll(Math.max(10000,20000/(i+1)),
                                TimeUnit.MILLISECONDS);
                        OMGraphicList tile = future.get();
                        if(tile.size() > 0 && tile.get(0) instanceof LegendRaster) {
                            LegendRaster legend = (LegendRaster) tile.get(0);
                            legend.setY(legendOffset);
                            legendOffset +=legend.getHeight();
                        }

                        result.addAll(tile);
                        tmpCache.putIfAbsent(getID(job), new OMGraphicList());
                        tmpCache.get(getID(job)).addAll(tile);
                        fireWMSEvent();
                    } catch (InterruptedException | ExecutionException
                            | NullPointerException e) {
                        allSuccess = false;
                        //LOG.debug("A Tile failed to download within the alotted time. (~5000ms)");
                    }
                }

                if (allSuccess) {
                    tmpCache.remove(getID(job));
                    cache.put(getID(job), result);
                    fireWMSEvent();
                } else {
                    LOG.debug("A Tile failed to download, resubmitting job");
                   // queue(job); perchè ripetere la cosa?
                }
            }
        });


    }

    public void queue(Projection p) {
        //Before check if it's in cache
        clearCache();
        if(cache.containsKey(getID(p))) {
            fireWMSEvent();

        }else if (this.projectionJobs.offer(p)) {
            LOG.info("Job offered");

        } else {
            LOG.debug("Queue is full, kicking old job in favor of new");
            try {
                this.projectionJobs.takeFirst();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.projectionJobs.offer(p);
        }

    }
    // La stringa è esattamente la key
    public String getID(Projection p) {
        LOG.info("Richiedo ID");
        return getQueryString(p) + "LEGEND=" + this.getWmsParams().getLegend();
    }
}
