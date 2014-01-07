package magicbox;

import java.util.*;



import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.exception.BACnetException;
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
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;


/**
 */

public class BacnetLogic {
	public static String BROADCAST_ADDRESS = "128.130.56.255";

	private final IpNetwork network;
	private final LocalDevice localDevice;
	public RemoteDevice remoteDevice;
	
	public ObjectIdentifier d_freigabeAnlagen = new ObjectIdentifier(ObjectType.binaryValue,2);
	public ObjectIdentifier d_sollwertHeizung= new ObjectIdentifier(ObjectType.analogValue,3);
	public ObjectIdentifier d_sollwertKalten = new ObjectIdentifier(ObjectType.analogValue,4);
	public ObjectIdentifier d_Stellen_Klappenantriebe = new ObjectIdentifier(ObjectType.analogOutput,6);
	public ObjectIdentifier d_M03_VG = new ObjectIdentifier(ObjectType.analogOutput,7);
	public ObjectIdentifier d_M05_VG = new ObjectIdentifier(ObjectType.analogOutput,8);
	public ObjectIdentifier d_M02_VG = new ObjectIdentifier(ObjectType.analogOutput,9);
	public ObjectIdentifier d_M04_VG = new ObjectIdentifier(ObjectType.analogOutput,10);
	public ObjectIdentifier d_temperature = new ObjectIdentifier(ObjectType.analogInput,3);
	public ObjectIdentifier d_A06_FRG_Durchlauf = new ObjectIdentifier(ObjectType.binaryOutput,8);
	public ObjectIdentifier d_A03_FRG_Kalten = new ObjectIdentifier(ObjectType.binaryOutput,9);
		
	public BacnetLogic()
			throws Exception {
		network = new IpNetwork(BROADCAST_ADDRESS, IpNetwork.DEFAULT_PORT);
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
			this.wait(2000);
			System.out.println(" waited for iAmReceived: "
					+ (System.currentTimeMillis() - start) + " ms");
		}
	}

	public void writeDevice(ObjectIdentifier p_oid, int p_value) throws BACnetException {
		RequestUtils.setProperty(localDevice, remoteDevice, p_oid,	
				PropertyIdentifier.presentValue, new Real(p_value));
		
		System.out.println("Writing to devices done...");
	}	
	
	public void writeDevice(ObjectIdentifier p_oid, boolean p_value) throws BACnetException {
		int val = p_value? 1 : 0;
		RequestUtils.setProperty(localDevice, remoteDevice, p_oid, 
				PropertyIdentifier.presentValue, new Enumerated(val));
		
		System.out.println("Writing to devices done...");
	}	
	
	@SuppressWarnings("unchecked")
	public Encodable readDevice(ObjectIdentifier p_oid) throws BACnetException {
		return RequestUtils.getProperty(localDevice, remoteDevice, p_oid, PropertyIdentifier.presentValue);
	}
	
	public void terminate(){
		localDevice.terminate();
		
	}
}