package magicbox;

import java.util.*;

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
import com.serotonin.bacnet4j.test.LoopDevice;

/**
 * Discovers and devices and print all properties of all objects found. this is
 * done by using PropertyIdentifier.all so the Device will send all propertys
 * that are set. if you want poll all PropertyId {@link ReadPropertyRangeTest}.
 * 
 * @author Matthew Lohbihler
 * @author Arne Pl�se
 */
public class BacnetLogic {
	public static String BROADCAST_ADDRESS = "128.130.56.255";
	private LoopDevice loopDevice;
	private final IpNetwork network;
	private final LocalDevice localDevice;
	// remote devices found
	final List<RemoteDevice> remoteDevices = new ArrayList<RemoteDevice>();
	public RemoteDevice remoteDevice;
	
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
				System.out.println("DiscoveryTest iAmReceived");
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

		// An other way to get to the list of devices
		// return localDevice.getRemoteDevices();
	}

	@SuppressWarnings("unchecked")
	private void printDevices() throws BACnetException {

			System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!\n"+remoteDevice.getObjects().size()+"\n"+remoteDevice.toString()+"\n");
			RequestUtils.getExtendedDeviceInformation(localDevice, remoteDevice);

			List<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) RequestUtils
					.sendReadPropertyAllowNull(localDevice, remoteDevice,
							remoteDevice.getObjectIdentifier(),
							PropertyIdentifier.objectList)).getValues();

			PropertyReferences refs = new PropertyReferences();
			// add the property references of the "device object" to the list
			refs.add(remoteDevice.getObjectIdentifier(), PropertyIdentifier.presentValue);
			System.out.println(remoteDevice.getObjectIdentifier()+"\n\n");

			// and now from all objects under the device object >> ai0,
			// ai1,bi0,bi1...
			for (ObjectIdentifier oid : oids) {
				refs.add(oid, PropertyIdentifier.presentValue);
			}

			System.out.println("Start read properties");
			final long start = System.currentTimeMillis();

			// Read values from refs
			
			Map<PropertyIdentifier,Encodable> properties = RequestUtils.getProperties(localDevice, 
					remoteDevice, null, PropertyIdentifier.presentValue);
			List<ObjectIdentifier> oidList = new ArrayList();
			oidList.add(new ObjectIdentifier(ObjectType.analogOutput,6));
			oidList.add(new ObjectIdentifier(ObjectType.analogValue,4));
			oidList.add(new ObjectIdentifier(ObjectType.analogValue,3));
			oidList.add(new ObjectIdentifier(ObjectType.analogInput,3));
			oidList.add(new ObjectIdentifier(ObjectType.binaryValue,2));
			
			PropertyValues propVal = RequestUtils.readOidPresentValues(localDevice, remoteDevice, oidList, null);
			try {
				Encodable prop =  propVal.get(oidList.get(4), PropertyIdentifier.presentValue);
				System.out.println("AC: "+prop.toString());
//				RequestUtils.sendReadPropertyAllowNull(localDevice, remoteDevice, 
//						new ObjectIdentifier(ObjectType.analogValue,3), PropertyIdentifier.presentValue);
				
				Real value = new Real(30);
				RequestUtils.setProperty(localDevice, remoteDevice,
						new ObjectIdentifier(ObjectType.binaryValue,2), 
						PropertyIdentifier.presentValue, new Enumerated(1));

				RequestUtils.setProperty(localDevice, remoteDevice,
						new ObjectIdentifier(ObjectType.analogValue,4), 
						PropertyIdentifier.presentValue, value);				
				
				
			} catch (PropertyValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PropertyValues pvs = RequestUtils.readProperties(localDevice, remoteDevice,
					refs, null);
			System.out.println(String.format("Properties read done in %d ms",
					System.currentTimeMillis() - start));
			
			//Print the read values
			printObject(remoteDevice.getObjectIdentifier(), pvs);
			for (ObjectIdentifier oid : oids) {
				printObject(oid, pvs);
			}


		System.out.println("Remote devices done...");
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
		
	private void printObject(ObjectIdentifier oid, PropertyValues pvs) {
		System.out.println(String.format("\t%s", oid));
		for (ObjectPropertyReference opr : pvs) {
			if (oid.equals(opr.getObjectIdentifier())) {
				System.out.println(String.format("\t\t%s = %s", opr
						.getPropertyIdentifier().toString(), pvs
						.getNoErrorCheck(opr)));
			}

		}
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
//			dt.printDevices();
			
			
//			Map<PropertyIdentifier,Encodable> properties = RequestUtils.getProperties(localDevice, 
//					remoteDevice, null, PropertyIdentifier.presentValue);
//			List<ObjectIdentifier> oidList = new ArrayList();
//			oidList.add(new ObjectIdentifier(ObjectType.analogOutput,6));
//			oidList.add(new ObjectIdentifier(ObjectType.analogValue,4));
//			oidList.add(new ObjectIdentifier(ObjectType.analogValue,3));
//			oidList.add(new ObjectIdentifier(ObjectType.analogInput,3));
//			oidList.add(new ObjectIdentifier(ObjectType.binaryValue,2));
//			
//			PropertyValues propVal = RequestUtils.readOidPresentValues(localDevice, remoteDevice, oidList, null);
//		
			
			
			
			dt.writeDevice(new ObjectIdentifier(ObjectType.binaryValue,2),1, true);
		} finally {
			dt.localDevice.terminate();
			System.out.println("Cleanup loopDevice");
			// dt.getLoopDevice().doTerminate();
		}
	}

	/**
	 * @return the loopDevice
	 */
	public LoopDevice getLoopDevice() {
		return loopDevice;
	}

	/**
	 * @param loopDevice
	 *            the loopDevice to set
	 */
	public void setLoopDevice(LoopDevice loopDevice) {
		this.loopDevice = loopDevice;
	}
}