package org.mobicents.protocols.ss7.m3ua.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javolution.util.FastList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.protocols.ss7.m3ua.impl.as.RemSigGatewayImpl;
import org.mobicents.protocols.ss7.m3ua.impl.parameter.ParameterFactoryImpl;
import org.mobicents.protocols.ss7.m3ua.impl.parameter.ProtocolDataImpl;
import org.mobicents.protocols.ss7.m3ua.impl.sg.SigGatewayImpl;
import org.mobicents.protocols.ss7.m3ua.message.transfer.PayloadData;
import org.mobicents.protocols.ss7.m3ua.parameter.DestinationPointCode;
import org.mobicents.protocols.ss7.m3ua.parameter.LocalRKIdentifier;
import org.mobicents.protocols.ss7.m3ua.parameter.RoutingContext;
import org.mobicents.protocols.ss7.m3ua.parameter.RoutingKey;
import org.mobicents.protocols.ss7.m3ua.parameter.ServiceIndicators;
import org.mobicents.protocols.ss7.m3ua.parameter.TrafficModeType;

public class GatewayTest {

    private ParameterFactoryImpl parmFactory = new ParameterFactoryImpl();

    As remAs;
    Asp remAsp;
    AspFactory remAspFactory;

    As localAs;
    Asp localAsp;
    AspFactory localAspFactory;

    private Server server;
    private Client client;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        RoutingContext rc = parmFactory.createRoutingContext(new long[] { 100 });

        DestinationPointCode[] dpc = new DestinationPointCode[] { parmFactory
                .createDestinationPointCode(123, (short) 0) };

        ServiceIndicators[] servInds = new ServiceIndicators[] { parmFactory.createServiceIndicators(new short[] { 3 }) };

        TrafficModeType trModType = parmFactory.createTrafficModeType(TrafficModeType.Override);
        LocalRKIdentifier lRkId = parmFactory.createLocalRKIdentifier(1);
        RoutingKey rKey = parmFactory.createRoutingKey(lRkId, rc, null, null, dpc, servInds, null);

        client = new Client(rc, rKey, trModType);
        server = new Server(rc, rKey, trModType);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSingleAspInAs() throws Exception {
        // 5.1.1. Single ASP in an Application Server ("1+0" sparing),

        System.out.println("Starting server");
        server.start();
        Thread.sleep(100);

        System.out.println("Starting Client");
        client.start();

        Thread.sleep(1000);

        // Both AS and ASP should be ACTIVE now
        assertEquals(remAsp.getState(), AspState.ACTIVE);
        assertEquals(remAs.getState(), AsState.ACTIVE);

        assertEquals(localAsp.getState(), AspState.ACTIVE);
        assertEquals(localAs.getState(), AsState.ACTIVE);

        client.sendPayload();
        server.sendPayload();

        Thread.sleep(1000);

        client.stop();
        //Give time to exchnge ASP_DOWN messages
        Thread.sleep(100);
        
        //The AS is Pending
        assertEquals(AsState.PENDING, localAs.getState());
        assertEquals(AsState.PENDING, remAs.getState());
        
        //Let the AS go in DOWN state
        Thread.sleep(2000);
        
        //The AS is Pending
        assertEquals(AsState.DOWN, localAs.getState());
        assertEquals(AsState.DOWN, remAs.getState());
        
        client.stopClient();
        server.stop();
        
        
        Thread.sleep(100);
        

        assertEquals(1, server.getReceivedData().size());
        assertEquals(1, client.getReceivedData().size());

    }

    private class Client implements Runnable {
        RoutingContext rc;
        RoutingKey rKey;
        TrafficModeType trModType;
        RemSigGatewayImpl rsgw;
        private FastList<PayloadData> receivedData = new FastList<PayloadData>();
        private volatile boolean started = false;

        public Client(RoutingContext rc, RoutingKey rKey, TrafficModeType trModType) {
            this.rc = rc;
            this.rKey = rKey;
            this.trModType = trModType;
        }

        public FastList<PayloadData> getReceivedData() {
            return receivedData;
        }

        public void start() throws Exception {
            // Set-up Rem Signaling Gateway
            rsgw = new RemSigGatewayImpl();
            rsgw.start();

            // m3ua as create rc <rc> <ras-name>
            localAs = rsgw.createAppServer("m3ua as create rc 100 client-testas".split(" "));
            // m3ua asp create ip <local-ip> port <local-port> remip <remip>
            // remport <remport> <asp-name>
            // localAspFactory = rsgw.createAspFactory("client-testasp",
            // "127.0.0.1", 3777, "127.0.0.1", 3112);
            localAspFactory = rsgw
                    .createAspFactory("m3ua asp create ip 127.0.0.1 port 3777 remip 127.0.0.1 remport 3112 client-testasp"
                            .split(" "));
            localAsp = rsgw.assignAspToAs("client-testas", "client-testasp");

            rsgw.startAsp("client-testasp");

            started = true;
            new Thread(this).start();
        }

        public void stop() throws Exception {
            rsgw.stopAsp("client-testasp");
            rsgw.stop();
        }
        
        public void stopClient(){
            started = false;
        }

        public void sendPayload() throws Exception {
            ProtocolDataImpl p1 = parmFactory.createProtocolData(1408, 123, 3, 1, 0, 1, new byte[] { 1, 2, 3, 4 });
            rsgw.send(p1.getMsu());
        }

        public void run() {
            while (started) {
                try {
                    rsgw.perform();
                    PayloadData payload = rsgw.poll();
                    if (payload != null) {
                        receivedData.add(payload);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class Server implements Runnable {
        RoutingContext rc;
        RoutingKey rKey;
        TrafficModeType trModType;
        SigGatewayImpl sgw;

        private volatile boolean started = false;

        private FastList<PayloadData> receivedData = new FastList<PayloadData>();

        public Server(RoutingContext rc, RoutingKey rKey, TrafficModeType trModType) {
            this.rc = rc;
            this.rKey = rKey;
            this.trModType = trModType;
        }

        public FastList<PayloadData> getReceivedData() {
            return receivedData;
        }

        public void start() throws Exception {
            // Set-up Signaling Gateway
            sgw = new SigGatewayImpl("127.0.0.1", 3112);
            sgw.start();

            // m3ua ras create rc <rc> rk dpc <dpc> opc <opc-list> si <si-list>
            // traffic-mode {broadcast|loadshare|override} <ras-name>
            remAs = sgw.createAppServer("m3ua ras create rc 100 rk dpc 123 si 3 traffic-mode override server-testas"
                    .split(" "));
            // m3ua rasp create ip <ip> port <port> <asp-name>"
            remAspFactory = sgw.createAspFactory("m3ua rasp create ip 127.0.0.1 port 3777 server-testasp".split(" "));
            remAsp = sgw.assignAspToAs("server-testas", "server-testasp");

            started = true;
            new Thread(this).start();
        }

        public void stop() throws IOException {
            started = false;
            sgw.stop();
        }

        public void sendPayload() throws Exception {
            ProtocolDataImpl p1 = parmFactory.createProtocolData(1408, 123, 3, 1, 0, 1, new byte[] { 1, 2, 3, 4 });
            sgw.send(p1.getMsu());
        }

        public void run() {
            while (started) {
                try {
                    sgw.perform();
                    PayloadData payload = sgw.poll();
                    if (payload != null) {
                        receivedData.add(payload);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}