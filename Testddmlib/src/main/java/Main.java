import com.android.ddmlib.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ragnarok on 16/5/9.
 */
public class Main {
    
    public static void main(String[] args) {
        AndroidDebugBridge.init(true);
        
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge("/Users/ragnarok/adt-bundle-mac-x86_64-20131030/sdk/platform-tools/adb", false);
        while (!bridge.isConnected() && !bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(200);
            }
            catch (Exception e) {
                System.out.println(e);
                return;
            }
        }
        
        System.out.println("Create bridge finished");
        System.out.println("isConnected: " + bridge.isConnected());
        System.out.println("devices: " + bridge.getDevices().length);
        if (bridge.getDevices() != null && bridge.getDevices().length > 0) {
            IDevice device = bridge.getDevices()[0];
            final Client client = device.getClient("com.example.ragnarok.allocrecordtest");
            System.out.print("client: " + client);
            if (client == null) {
                return;
            }
            AndroidDebugBridge.addClientChangeListener(new AndroidDebugBridge.IClientChangeListener() {
                public void clientChanged(Client cl, int changeMask) {
                    if (cl == client) {
                        if ((changeMask & Client.CHANGE_HEAP_ALLOCATIONS) != 0) {
                            System.out.println("clientChanged: " + cl + ", " + changeMask);
                            byte[] data = cl.getClientData().getAllocationsData();
                            if (data != null) {
                                AllocationInfo[] infoList = AllocationsParser.parse(ByteBuffer.wrap(data));
                                System.out.println("infoList size: " + infoList.length);
                                if (infoList.length > 0) {
                                    AllocationInfo.AllocationSorter sorter = new AllocationInfo.AllocationSorter();
                                    sorter.setSortMode(AllocationInfo.SortMode.SIZE, true);
                                    Arrays.sort(infoList, sorter);
                                    System.out.println(infoList[0].getAllocatedClass());
                                    printAllocationInfo(infoList[0]);
                                }
                            }
                        }
                    }
                }
            });
            
            System.out.println("start allocation tracker");
            client.enableAllocationTracker(true);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    client.requestAllocationDetails();
                    client.enableAllocationTracker(false);
                }
            }, 3000);
        }
        
        
    }
    
    private static void printAllocationInfo(AllocationInfo allocationInfo) {
        if (allocationInfo != null) {
            StringBuilder printResult = new StringBuilder();
            printResult.append("class: ");
            printResult.append(allocationInfo.getAllocatedClass());
            printResult.append(", size: ");
            printResult.append(allocationInfo.getSize());
            printResult.append(", number: ");
            printResult.append(allocationInfo.getAllocNumber());
            printResult.append(", stacktrace: ");
            printResult.append(allocationInfo.getStackTrace()[0]);
            System.out.println(printResult);
        }
    }
}
