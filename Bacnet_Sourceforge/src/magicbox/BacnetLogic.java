package magicbox;

import java.util.*;

import org.omg.CORBA.FREE_MEM;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.PropertyValueException;
import com.serotonin.bacnet4j.npdu.ip.InetAddrCache;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.confirmed.ReinitializeDeviceRequest.ReinitializedStateOfDevice;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.Choice;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.TimeStamp;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Primitive;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;


/**
 */

public class BacnetLogic {
	public static String BROADCAST_ADDRESS = "128.130.56.255";

	private final IpNetwork network;
	private final LocalDevice localDevice;
	// remote devices found
	public RemoteDevice remoteDevice;
	
	private static ObjectIdentifier d_freigabeAnlagen = new ObjectIdentifier(ObjectType.binaryValue,2);

	private static ObjectIdentifier d_sollwertHeizung= new ObjectIdentifier(ObjectType.analogValue,3);
	private static ObjectIdentifier d_sollvertKalten = new ObjectIdentifier(ObjectType.analogValue,4);
	
	private static ObjectIdentifier d_Stellen_Klappenantriebe = new ObjectIdentifier(ObjectType.analogOutput,6);
	private static ObjectIdentifier d_M03_VG = new ObjectIdentifier(ObjectType.analogOutput,7);
	private static ObjectIdentifier d_M05_VG = new ObjectIdentifier(ObjectType.analogOutput,8);
	private static ObjectIdentifier d_M02_VG = new ObjectIdentifier(ObjectType.analogOutput,9);
	private static ObjectIdentifier d_M04_VG = new ObjectIdentifier(ObjectType.analogOutput,10);
	
	private static ObjectIdentifier d_temperature = new ObjectIdentifier(ObjectType.analogInput,3);
	
	private static ObjectIdentifier d_A06_FRG_Durchlauf = new ObjectIdentifier(ObjectType.binaryOutput,8);
	private static ObjectIdentifier d_A03_FRG_Kalten = new ObjectIdentifier(ObjectType.binaryOutput,9);
	

	
	public BacnetLogic(String broadcastAddress, int port)
			throws Exception {
		network = new IpNetwork(broadcastAddress, port);
		localDevice = new LocalDevice(2323, new Transport(network));
		localDevice.getEventHandler().addListener(new DeviceEventListener() {

			@Override
			public void listenerException(Throwable e) {
				System.out.println("DiscoveryTest listenerException");
			}

			@Override
			public void iAmReceived(RemoteDevice d) {
				// Find our device 2098177
				if (d.getInstanceNumber() == 2098177) {
					//remoteDevices.add(d);
					remoteDevice = d;
					synchronized (BacnetLogic.this) {
						BacnetLogic.this.notifyAll();
					}
				}
			}

			@Override
			public boolean allowPropertyWrite(BACnetObject obj, PropertyValue pv) {
				System.out.println("DiscoveryTest allowPropertyWrite");
				return true;
			}

			@Override
			public void propertyWritten(BACnetObject obj, PropertyValue pv) {
				System.out.println("DiscoveryTest propertyWritten");
			}

			@Override
			public void iHaveReceived(RemoteDevice d, RemoteObject o) {
				System.out.println("DiscoveryTest iHaveReceived");
			}

			@Override
			public void covNotificationReceived(
					UnsignedInteger subscriberProcessIdentifier,
					RemoteDevice initiatingDevice,
					ObjectIdentifier monitoredObjectIdentifier,
					UnsignedInteger timeRemaining,
					SequenceOf<PropertyValue> listOfValues) {
				System.out.println("DiscoveryTest covNotificationReceived");
			}

			@Override
			public void eventNotificationReceived(
					UnsignedInteger processIdentifier,
					RemoteDevice initiatingDevice,
					ObjectIdentifier eventObjectIdentifier,
					TimeStamp timeStamp, UnsignedInteger notificationClass,
					UnsignedInteger priority, EventType eventType,
					CharacterString messageText, NotifyType notifyType,
					Boolean ackRequired, EventState fromState,
					EventState toState, NotificationParameters eventValues) {
				System.out.println("DiscoveryTest eventNotificationReceived");
			}

			@Override
			public void textMessageReceived(
					RemoteDevice textMessageSourceDevice, Choice messageClass,
					MessagePriority messagePriority, CharacterString message) {
				System.out.println("DiscoveryTest textMessageReceived");
			}

			@Override
			public void privateTransferReceived(UnsignedInteger vendorId,
					UnsignedInteger serviceNumber, Encodable serviceParameters) {
				System.out.println("DiscoveryTest privateTransferReceived");
			}

			@Override
			public void reinitializeDevice(
					ReinitializedStateOfDevice reinitializedStateOfDevice) {
				System.out.println("DiscoveryTest reinitializeDevice");
			}

			@Override
			public void synchronizeTime(DateTime dateTime, boolean utc) {
				System.out.println("DiscoveryTest synchronizeTime");
			}
		});

		localDevice.initialize();
	}

	/**
	 * Send a WhoIs request and wait for the first to answer
	 * 
	 * @throws java.lang.Exception
	 */
	public void doDiscover() throws Exception {
		// Who is
		System.out.println("Send Broadcast WhoIsRequest() ");
		// Send the broadcast to the correct port of the LoopDevice !!!
		localDevice.sendBroadcast(
				new Address(InetAddrCache.get(BROADCAST_ADDRESS, 0xBAC0)),
				null, new WhoIsRequest(null, null));

		// wait for notification in iAmReceived() Timeout 2 sec
		synchronized (this) {
			final long start = System.currentTimeMillis();
			this.wait(3000);
			System.out.println(" waited for iAmReceived: "
					+ (System.currentTimeMillis() - start) + " ms");
		}
	}

	private void writeDevice(ObjectIdentifier p_oid, int p_value, boolean p_bool) throws BACnetException {
		if (p_bool == true) {
			RequestUtils.setProperty(localDevice, remoteDevice, p_oid, 
					PropertyIdentifier.presentValue, new Enumerated(p_value));
		}
		else {
			RequestUtils.setProperty(localDevice, remoteDevice, p_oid,	
					PropertyIdentifier.presentValue, new Real(p_value));				
		}
		System.out.println("Writing to devices done...");
	}	
	
	@SuppressWarnings("unchecked")
	private Encodable readDevice(ObjectIdentifier p_oid) throws BACnetException {
		return RequestUtils.getProperty(localDevice, remoteDevice, p_oid, PropertyIdentifier.presentValue);
	}


	/**
	 * Note same Broadcast address, but different ports!!!
	 * 
	 * @param args
	 * @throws java.lang.Exception
	 */
	public static void main(String[] args) throws Exception {
		BacnetLogic dt = new BacnetLogic(
				BROADCAST_ADDRESS, IpNetwork.DEFAULT_PORT);
		try {
			// dt.setLoopDevice(new LoopDevice(BROADCAST_ADDRESS,
			// IpNetwork.DEFAULT_PORT + 1));
		} catch (RuntimeException e) {
			dt.localDevice.terminate();
			throw e;
		}
		try {
			dt.doDiscover();
			
			Encodable test = dt.readDevice(new ObjectIdentifier(ObjectType.analogValue,4));
			System.out.println(test.toString());

			dt.writeDevice(d_freigabeAnlagen ,0, true);
			dt.writeDevice(d_sollvertKalten ,50, false);
			dt.writeDevice(d_sollwertHeizung ,10, false);


			
		} finally {
			dt.localDevice.terminate();
			System.out.println("Cleanup loopDevice");
			// dt.getLoopDevice().doTerminate();
		}
	}


}