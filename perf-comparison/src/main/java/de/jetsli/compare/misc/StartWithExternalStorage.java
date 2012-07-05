/*
 *  Copyright 2012 Peter Karich info@jetsli.de
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetsli.compare.misc;

import de.jetsli.compare.neo4j.Neo4JStorage;
import de.jetsli.graph.reader.OSMReader;
import de.jetsli.graph.storage.Storage;
import de.jetsli.graph.storage.Graph;
import de.jetsli.graph.util.CmdArgs;
import de.jetsli.graph.util.Helper;
import de.jetsli.graph.util.XFirstSearch;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Karich
 */
public class StartWithExternalStorage {

    public static void main(String[] args) throws Exception {
        new StartWithExternalStorage().start(Helper.readCmdArgs(args));
    }
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void start(CmdArgs readCmdArgs) throws Exception {
        int initSize = readCmdArgs.getInt("size", 5000000);
         final Storage s = new Neo4JStorage(readCmdArgs.get("storage", "neo4j.db"), initSize);
//        final Storage s = new TinkerStorage(readCmdArgs.get("storage", "tinker.db"), initSize);
        OSMReader reader = new OSMReader(null, initSize) {

            @Override protected Storage createStorage(String storageLocation, int size) {
                return s;
            }
        };
        Graph g = OSMReader.osm2Graph(reader, readCmdArgs);
        logger.info("finished with locations:" + g.getNodes());
        final AtomicInteger integ = new AtomicInteger(0);
        new XFirstSearch() {

            @Override
            protected boolean goFurther(int nodeId) {
                integ.incrementAndGet();
                return super.goFurther(nodeId);
            }
        }.start(g, 0, true);

        logger.info(integ.get() + " <- all reachable nodes");
        reader.doDijkstra(1000);
    }
}