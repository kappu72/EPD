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
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import it.toscana.rete.lamma.prototype.metocservices.WMSMetocLayers;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;


import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TiledWMSTimeService extends AbstractWMSTimeService {

    protected int root;
    protected int sqrRoot;

    @SuppressWarnings("unused")
    private Projection projection;

    public TiledWMSTimeService(String wmsQuery, int tileNumber) {
        super(wmsQuery);
        this.root = tileNumber;
        this.sqrRoot = (int) Math.sqrt(root);
    }
    /**
     *
     * @param p projection
     * @return list of rasterizable image tiles
     */
    protected final Collection<SingleWMSTimeService> getTiles(Projection p) {
        LinkedList<SingleWMSTimeService> l = new LinkedList<>();
        LammaMetocWMSConfig params = this.getWmsParams();
        l.add(new SingleWMSTimeService(wmsQuery, p.makeClone(), params));
        if(params.getLegend()){
            // Adds legend layers
            params.getLayers().stream()
                    .forEach(layer -> l.add(new WMSLayerLegendService(wmsQuery, p.makeClone(), params,layer)));
        }
        return l;
    }

    private void setProjection(final Projection p) {
        this.projection = p;

    }

    @Override
    public OMGraphicList getWmsList(Projection p) {

        OMGraphicList result = new OMGraphicList();

        // why not make this a field? because we want total separation between
        // results
        ExecutorService es = Executors.newCachedThreadPool();

        Collection<SingleWMSTimeService> wmsInstances = this.getTiles(p);

        try {
            List<Future<OMGraphicList>> futures = es.invokeAll(wmsInstances,
                    10, TimeUnit.SECONDS);

            for (Future<OMGraphicList> f : futures) {
                try {
                    result.addAll(f.get());
//
                } catch (CancellationException e) {
                    LOG.debug("WMS TILE CANCELLED");
                }

            }

        } catch (InterruptedException | ExecutionException e) {

        }

        // Singlethreaded alternative
        /*
         * for (SingleWMSService s: wmsInstances) {
         * result.addAll(s.getWmsList(p)); }
         */

        return result;

    }

}
