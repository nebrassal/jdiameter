/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.slee.resource.diameter.base.events;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.slee.resource.diameter.base.events.DiameterCommand;
import net.java.slee.resource.diameter.base.events.DiameterHeader;
import net.java.slee.resource.diameter.base.events.DiameterMessage;
import net.java.slee.resource.diameter.base.events.avp.AddressAvp;
import net.java.slee.resource.diameter.base.events.avp.AvpNotAllowedException;
import net.java.slee.resource.diameter.base.events.avp.AvpUtilities;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvp;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvpType;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentityAvp;
import net.java.slee.resource.diameter.base.events.avp.DiameterURIAvp;
import net.java.slee.resource.diameter.base.events.avp.FailedAvp;
import net.java.slee.resource.diameter.base.events.avp.GroupedAvp;
import net.java.slee.resource.diameter.base.events.avp.ProxyInfoAvp;
import net.java.slee.resource.diameter.base.events.avp.RedirectHostUsageType;
import net.java.slee.resource.diameter.base.events.avp.VendorSpecificApplicationIdAvp;

import org.apache.log4j.Logger;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;
import org.mobicents.slee.resource.diameter.base.events.avp.AddressAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.DiameterAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.DiameterIdentityAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.DiameterURIAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.FailedAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.ProxyInfoAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.VendorSpecificApplicationIdAvpImpl;

/**
 * Super class for all diameter messages <br>
 * <br>
 * Super project: mobicents <br>
 * 13:25:46 2008-05-08 <br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author Erick Svenson
 */
public abstract class DiameterMessageImpl implements DiameterMessage {

	private Logger log = Logger.getLogger(DiameterMessageImpl.class);

	protected Message message = null;

	/**
	 * Constructor taking a jDiameter {@link Message} as argument.
	 * 
	 * @param message the jDiameter Message object to create the DiameterMessage from
	 */
  public DiameterMessageImpl(Message message)
  {
    this.message = message;
  }

	// Begin of DiameterMessage Implementation
	
  public DiameterHeader getHeader()
  {
    return new DiameterHeaderImpl(this.message);
  }

  public DiameterCommand getCommand()
  {
    return new DiameterCommandImpl(this.message.getCommandCode(), this.message.getApplicationId(), getShortName(), getLongName(), this.message.isRequest(), this.message.isProxiable());
  }

  public DiameterAvp[] getAvps()
  {
  	DiameterAvp[] avps = new DiameterAvp[0];
  
  	try {
  		avps = getAvpsInternal(message.getAvps());
  	}
  	catch (Exception e) {
  		log.error("Failed to obtain/decode AVP/data.", e);
  	}
  
  	return avps;
  }

  public String getSessionId()
  {
  	return message.getSessionId();
  }

  public boolean hasSessionId()
  {
    return hasAvp(Avp.SESSION_ID);
  }

  public void setSessionId(String sessionId)
  {
  	if (!hasSessionId()) {
  		this.message.getAvps().addAvp(Avp.SESSION_ID, sessionId, true, false, true);
  	}
  	else {
  		this.message.getAvps().removeAvp(Avp.SESSION_ID);
  		this.message.getAvps().addAvp(Avp.SESSION_ID, sessionId, true, false, true);
  		// throw new
  		// IllegalStateException("Unable to set Session-Id AVP. Already set.");
  	}
  }

  public DiameterIdentityAvp getOriginHost()
  {
  	return getAvpAsIdentity(Avp.ORIGIN_HOST);
  }

  public boolean hasOriginHost()
  {
  	return hasAvp(Avp.ORIGIN_HOST);
  }

  public void setOriginHost(DiameterIdentityAvp originHost)
  {
  	if (!hasOriginHost()) {
  		this.message.getAvps().addAvp(Avp.ORIGIN_HOST, originHost.stringValue(), true, false, true);
  	}
  	else {
  		throw new IllegalStateException("Unable to set Origin-Host AVP. Already set.");
  	}
  }

  public DiameterIdentityAvp getOriginRealm()
  {
  	return getAvpAsIdentity(Avp.ORIGIN_REALM);
  }

  public boolean hasOriginRealm()
  {
  	return hasAvp(Avp.ORIGIN_REALM);
  }

  public void setOriginRealm(DiameterIdentityAvp originRealm)
  {
  	if (!hasOriginRealm()) {
  		this.message.getAvps().addAvp(Avp.ORIGIN_REALM, originRealm.stringValue(), true, false, true);
  	}
  	else {
  		throw new IllegalStateException("Unable to set Origin-Realm AVP. Already set.");
  	}
  }

  public DiameterIdentityAvp getDestinationHost()
  {
  	return getAvpAsIdentity(Avp.DESTINATION_HOST);
  }

  public void setDestinationHost(DiameterIdentityAvp destinationHost) {
  	if (!hasDestinationHost()) {
  		this.message.getAvps().addAvp(Avp.DESTINATION_HOST, destinationHost.stringValue(), true, false, true);
  	} else {
  		throw new IllegalStateException("Unable to set Destination-Host AVP. Already set.");
  	}
  }

  public DiameterIdentityAvp getDestinationRealm()
  {
  	return getAvpAsIdentity(Avp.DESTINATION_REALM);
  }

  public void setDestinationRealm(DiameterIdentityAvp destinationRealm) {
  	if (!hasDestinationRealm()) {
  		this.message.getAvps().addAvp(Avp.DESTINATION_REALM, destinationRealm.stringValue(), true, false, true);
  	} else {
  		throw new IllegalStateException("Unable to set Destination-Realm AVP. Already set.");
  	}
  }

  public DiameterAvp[] getExtensionAvps() {
  	return getAvps();
  }

  public void setExtensionAvps(DiameterAvp... avps) throws AvpNotAllowedException {
  	for (DiameterAvp a : avps) {
  		this.addAvp(a);
  	}
  }

  // End of DiameterMessage Implementation
  
	protected void setAvpAsByteArray(int code, long vendorId, byte[] value, boolean mandatory, boolean _protected) {
		message.getAvps().addAvp(code, value, mandatory, _protected);
	}

	protected void setAvpAsByteArray(int code, long vendorId, byte[] value, boolean mandatory) {
		this.setAvpAsByteArray(code, vendorId, value, mandatory, false);
	}

	public Object clone() {
		// TODO
		return null;
	}

	// ======== GETTERS
	protected AddressAvp[] getAvpAsAddress(int code) {
		AvpSet avps = message.getAvps().getAvps(code);

		if (avps == null)
			return null;

		AddressAvp[] r = new AddressAvp[avps.size()];

		for (int i = 0; i < avps.size(); i++) {
			try {
				r[i] = AddressAvpImpl.decode(avps.getAvpByIndex(i).getRaw());
			} catch (AvpDataException e) {
				log.error("Failed to decode AVP data at index[" + i + "] (code: " + code + ")", e);
				return null;
			}
		}

		return r;
	}

	public long getAcctApplicationId() {
		return getAvpAsUInt32(Avp.ACCT_APPLICATION_ID);
	}

	protected DiameterIdentityAvp[] getAllAvpAsIdentity(int code) {
		List<DiameterIdentityAvp> acc = new ArrayList<DiameterIdentityAvp>();

		for (Avp a : message.getAvps().getAvps(code)) {
			try {
				acc.add(new DiameterIdentityAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw()));
			} catch (Exception e) {
				log.error("Failed to decode AVP data. (code: " + code + ")", e);
				return null;
			}
		}

		return acc.toArray(new DiameterIdentityAvp[0]);
	}

	public long getAuthApplicationId() {
		return getAvpAsUInt32(Avp.AUTH_APPLICATION_ID);
	}

	protected Date getAvpAsDate(int code) {
		try {
			return message.getAvps().getAvp(code).getTime();
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return null;
		}
	}

	protected DiameterIdentityAvp getAvpAsIdentity(int code) {
		try {
			Avp rawAvp = message.getAvps().getAvp(code);

			return rawAvp != null ? new DiameterIdentityAvpImpl(rawAvp.getCode(), rawAvp.getVendorId(), rawAvp.isMandatory() ? 1 : 0, rawAvp.isEncrypted() ? 1 : 0, rawAvp.getRaw()) : null;
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return null;
		}
	}

	protected int getAvpAsInt32(int code) {
		try {
			return message.getAvps().getAvp(code).getInteger32();
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return -1;
		}
	}

	protected long getAvpAsUInt32(int code) {
		try {
			return message.getAvps().getAvp(code).getUnsigned32();
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return -1;
		}
	}

	protected String getAvpAsUtf8(int code) {
		try {
			return message.getAvps().getAvp(code).getUTF8String();
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return null;
		}
	}
	protected String getAvpAsOctet(int code) {
		try {
			return message.getAvps().getAvp(code).getOctetString();
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + code + ")", e);
			return null;
		}
	}
	protected long[] getAvpsAsUInt32(int code) {
		AvpSet avps = message.getAvps().getAvps(code);

		if (avps != null) {
			long[] r = new long[avps.size()];
			for (int i = 0; i < avps.size(); i++) {
				try {
					r[i] = avps.getAvpByIndex(i).getUnsigned32();
				} catch (AvpDataException e) {
					log.error("Failed to decode AVP data. (code: " + code + ")", e);
					return null;
				}
			}

			return r;
		} else {
			return null;
		}
	}

	protected int[] getAvpsAsInt32(int code) {
		AvpSet avps = message.getAvps().getAvps(code);

		if (avps != null) {
			int[] r = new int[avps.size()];
			for (int i = 0; i < avps.size(); i++) {
				try {
					r[i] = avps.getAvpByIndex(i).getInteger32();
				} catch (AvpDataException e) {
					log.error("Failed to decode AVP data. (code: " + code + ")", e);
					return null;
				}
			}

			return r;
		} else {
			return null;
		}
	}

	public String getErrorMessage() {
		return getAvpAsUtf8(Avp.ERROR_MESSAGE);
	}

	public DiameterIdentityAvp getErrorReportingHost() {
		return getAvpAsIdentity(Avp.ERROR_REPORTING_HOST);
	}

	public Date getEventTimestamp() {
		return getAvpAsDate(Avp.EVENT_TIMESTAMP);
	}

	public FailedAvp[] getFailedAvps() {
		if (hasFailedAvp()) {
			List<FailedAvp> acc = new ArrayList<FailedAvp>();

			for (Avp a : message.getAvps().getAvps(Avp.FAILED_AVP)) {
				try {
					acc.add(new FailedAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw()));
				} catch (Exception e) {
					log.error("Failed to decode AVP data. (code: " + a.getCode() + ")", e);
				}
			}
			return acc.toArray(new FailedAvp[0]);
		} else {
			return null;
		}
	}

	public FailedAvp getFailedAvp() {
		if (hasFailedAvp()) {
			
			Avp a = message.getAvps().getAvp(Avp.FAILED_AVP);
			try {
				return new FailedAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw());
			} catch (AvpDataException e) {
				log.error("Failed to decode AVP data. (code: " + a.getCode() + ")", e);
				return null;
			}
		
		} else {
			return null;
		}
	}

	public Message getGenericData() {
		return message;
	}

	/**
	 * This method returns long name of this message type - Like
	 * Device-Watchdog-Request
	 * 
	 * @return
	 */
	public abstract String getLongName();

	public long getOriginStateId() {
		return getAvpAsUInt32(Avp.ORIGIN_STATE_ID);
	}

	public ProxyInfoAvp[] getProxyInfos() {
		List<ProxyInfoAvp> acc = new ArrayList<ProxyInfoAvp>();

		for (Avp a : message.getAvps().getAvps(Avp.PROXY_INFO)) {
			try {
				acc.add(new ProxyInfoAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw()));
			} catch (Exception e) {
				log.error("Failed to decode AVP data. (code: " + a.getCode() + ")", e);
			}
		}

		return acc.toArray(new ProxyInfoAvp[0]);
	}

	public DiameterURIAvp[] getRedirectHosts() {
		List<DiameterURIAvp> acc = new ArrayList<DiameterURIAvp>();

		for (DiameterIdentityAvp a : getAllAvpAsIdentity(Avp.REDIRECT_HOST)) {
			try {
				acc.add(new DiameterURIAvpImpl(a.toString()));
			} catch (URISyntaxException e) {
				log.error("Failed to decode AVP data. (code: " + a.getCode() + ")", e);
			}
		}

		return acc.toArray(new DiameterURIAvpImpl[0]);
	}

	public RedirectHostUsageType getRedirectHostUsage() {
		return RedirectHostUsageType.fromInt(getAvpAsInt32(Avp.REDIRECT_HOST_USAGE));
	}

	public long getRedirectMaxCacheTime() {
		return getAvpAsUInt32(Avp.REDIRECT_MAX_CACHE_TIME);
	}

	public long getResultCode() {
		// This method is visible only if interface defines it :)
		return getAvpAsUInt32(Avp.RESULT_CODE);
	}

	public DiameterIdentityAvp[] getRouteRecords() {
		return getAllAvpAsIdentity(Avp.ROUTE_RECORD);
	}

	/**
	 * This method return short name of this message type - for instance DWR,DWA
	 * for DeviceWatchdog message
	 * 
	 * @return
	 */
	public abstract String getShortName();

	public String getUserName() {
		return getAvpAsUtf8(Avp.USER_NAME);
	}

	public VendorSpecificApplicationIdAvp getVendorSpecificApplicationId() {
		try {
			Avp a = message.getAvps().getAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);

			return new VendorSpecificApplicationIdAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw());
		} catch (Exception e) {
			log.error("Failed to decode AVP data. (code: " + Avp.VENDOR_SPECIFIC_APPLICATION_ID + ")", e);
			return null;
		}
	}

	// -------- HAS SECTION

	public boolean hasAcctApplicationId() {
		return hasAvp(Avp.ACCT_APPLICATION_ID);
	}

	public boolean hasAuthApplicationId() {
		return hasAvp(Avp.AUTH_APPLICATION_ID);
	}

	public boolean hasDestinationHost() {
		return hasAvp(Avp.DESTINATION_HOST);
	}

	public boolean hasDestinationRealm() {
		return hasAvp(Avp.DESTINATION_REALM);
	}

	public boolean hasErrorMessage() {
		return hasAvp(Avp.ERROR_MESSAGE);
	}

	public boolean hasErrorReportingHost() {
		return hasAvp(Avp.ERROR_REPORTING_HOST);
	}

	public boolean hasEventTimestamp() {
		return hasAvp(Avp.EVENT_TIMESTAMP);
	}

	public boolean hasOriginStateId() {
		return hasAvp(Avp.ORIGIN_STATE_ID);
	}

	public boolean hasRedirectHostUsage() {
		return hasAvp(Avp.REDIRECT_HOST_USAGE);
	}

	public boolean hasRedirectMaxCacheTime() {
		return hasAvp(Avp.REDIRECT_MAX_CACHE_TIME);
	}

	public boolean hasResultCode() {
		return hasAvp(Avp.RESULT_CODE);
	}

	public boolean hasUserName() {
		return hasAvp(Avp.USER_NAME);
	}

	public boolean hasVendorSpecificApplicationId() {
		return hasAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
	}

	public boolean hasProxyInfo() {
		return hasAvp(Avp.PROXY_INFO);
	}

	public boolean hasFailedAvp() {
		return hasAvp(Avp.FAILED_AVP);
	}

	// =============== SETTERS

  public void addAvp(String avpName, Object avp)
  {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpName);
    
    if(rep != null)
    {
      addAvp(rep.getCode(), rep.getVendorId(), avp);
    }
  }

  public void addAvp(int avpCode, Object avp)
  {
    addAvp(avpCode, 0, avp );
  }
	
  public void addAvp(int avpCode, long vendorId, Object avp)
  {
    AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);
    
    if(avpRep != null)
    {
      DiameterAvpType avpType = DiameterAvpType.fromString(avpRep.getType());
      
      boolean isMandatoryAvp = !(avpRep.getRuleMandatory().equals("mustnot") || avpRep.getRuleMandatory().equals("shouldnot"));
      boolean isProtectedAvp = avpRep.getRuleProtected().equals("must");
      
      if(avp instanceof byte[])
      {
        setAvpAsByteArray(avpCode, vendorId, (byte[]) avp, isMandatoryAvp, isProtectedAvp);
      }
      else
      {
        switch (avpType.getType())
        {
        case DiameterAvpType._ADDRESS:
        case DiameterAvpType._DIAMETER_IDENTITY:
        case DiameterAvpType._DIAMETER_URI:
        case DiameterAvpType._IP_FILTER_RULE:
        case DiameterAvpType._OCTET_STRING:
        case DiameterAvpType._QOS_FILTER_RULE:
        {
          setAvpAsOctetString(avpCode, vendorId, (String) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        case DiameterAvpType._ENUMERATED:
        case DiameterAvpType._INTEGER_32:
        {
          setAvpAsInteger32(avpCode, vendorId, (Integer) avp, isMandatoryAvp, isProtectedAvp);        
          break;
        }
        case DiameterAvpType._FLOAT_32:
        {
          setAvpAsFloat32(avpCode, vendorId, (Float) avp, isMandatoryAvp, isProtectedAvp);        
          break;
        }
        case DiameterAvpType._FLOAT_64:
        {
          setAvpAsFloat64(avpCode, vendorId, (Float) avp, isMandatoryAvp, isProtectedAvp);        
          break;
        }
        case DiameterAvpType._GROUPED:
        {
          setAvpAsGrouped(avpCode, vendorId, (DiameterAvp[]) avp, isMandatoryAvp, isProtectedAvp);        
          break;
        }
        case DiameterAvpType._INTEGER_64:
        {
          setAvpAsInteger64(avpCode, vendorId, (Integer) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        case DiameterAvpType._TIME:
        {
          setAvpAsTime(avpCode, vendorId, (Date) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        case DiameterAvpType._UNSIGNED_32:
        {
          setAvpAsUnsigned32(avpCode, vendorId, (Long) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        case DiameterAvpType._UNSIGNED_64:
        {
          setAvpAsUnsigned64(avpCode, vendorId, (Long) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        case DiameterAvpType._UTF8_STRING:
        {
          setAvpAsUTF8String(avpCode, vendorId, (String) avp, isMandatoryAvp, isProtectedAvp);
          break;
        }
        }
      }
    }
  }

	public void setAcctApplicationId(long acctApplicationId) {
		addAvp(Avp.ACCT_APPLICATION_ID, acctApplicationId);
	}

	public void setAuthApplicationId(long authApplicationId) {
	  addAvp(Avp.AUTH_APPLICATION_ID, authApplicationId);
	}

  protected void setAvpAsTime(int code, long vendorId, Date value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsTime(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

  protected void setAvpAsFloat32(int code, long vendorId, float value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsFloat32(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

  protected void setAvpAsFloat64(int code, long vendorId, float value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsFloat64(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

	protected AvpSet setAvpAsGrouped(int code, long vendorId, DiameterAvp[] childs, boolean isMandatory, boolean isProtected)
	{
		return AvpUtilities.setAvpAsGrouped(code, vendorId, childs, message.getAvps(), isMandatory, isProtected);
	}

  protected void setAvpAsInteger32(int code, long vendorId, int value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsInteger32(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

  protected void setAvpAsInteger64(int code, long vendorId, long value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsInteger64(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

  protected void setAvpAsUnsigned32(int code, long vendorId, long value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsUnsigned32(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

  protected void setAvpAsUnsigned64(int code, long vendorId, long value, boolean isMandatory, boolean isProtected)
  {
    AvpUtilities.setAvpAsUnsigned64(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
  }

	protected void setAvpAsUTF8String(int code, long vendorId, String value, boolean isMandatory, boolean isProtected)
	{
		AvpUtilities.setAvpAsUTF8String(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
	}
	
	protected void setAvpAsOctetString(int code, long vendorId, String value, boolean isMandatory, boolean isProtected)
	{
		AvpUtilities.setAvpAsOctetString(code, vendorId, message.getAvps(), isMandatory, isProtected, value);
	}
	
	public void setErrorMessage(String errorMessage) {
		addAvp(Avp.USER_NAME, errorMessage);
	}

	public void setErrorReportingHost(DiameterIdentityAvp errorReportingHost) {
		addAvp(Avp.ERROR_REPORTING_HOST, errorReportingHost.toString());
	}

	public void setEventTimestamp(Date eventTimestamp) {
		addAvp(Avp.EVENT_TIMESTAMP, eventTimestamp);
	}

	public void setFailedAvp(FailedAvp failedAvp) {
	  addAvp(Avp.FAILED_AVP, failedAvp.getExtensionAvps());
	}

	public void setFailedAvps(FailedAvp[] failedAvps)
	{
		for (FailedAvp f : failedAvps) {
			setFailedAvp(f);
		}
	}

	public void setOriginStateId(long originStateId)
	{
	  addAvp(Avp.ORIGIN_STATE_ID, originStateId);
	}

	public void setProxyInfo(ProxyInfoAvp proxyInfo)
	{
    // FIXME: Alexandre: Make it use addAvp(...)
		AvpSet g = setAvpAsGrouped(Avp.PROXY_INFO, 0, proxyInfo.getExtensionAvps(), true, false);

		if (proxyInfo.hasProxyHost()) {
			g.addAvp(Avp.PROXY_HOST, proxyInfo.getProxyHost().toString(), true, true, false);
		}

		if (proxyInfo.hasProxyState()) {
			g.addAvp(Avp.PROXY_STATE, proxyInfo.getProxyState(), true, false);
		}
	}

	public void setProxyInfos(ProxyInfoAvp[] proxyInfos) {
		for (ProxyInfoAvp p : proxyInfos) {
			setProxyInfo(p);
		}
	}

	public void setRedirectHost(DiameterURIAvp redirectHost) {
		addAvp(Avp.REDIRECT_HOST, redirectHost.toString());
	}

	public void setRedirectHosts(DiameterURIAvp[] redirectHosts) {
		for (DiameterURIAvp uri : redirectHosts) {
			setRedirectHost(uri);
		}
	}

	public void setRedirectHostUsage(RedirectHostUsageType redirectHostUsage) {
	  addAvp(Avp.REDIRECT_HOST_USAGE, redirectHostUsage.getValue());
	}

	public void setRedirectMaxCacheTime(long redirectMaxCacheTime) {
	  addAvp(Avp.REDIRECT_MAX_CACHE_TIME, redirectMaxCacheTime);
	}

	public void setResultCode(long resultCode) {
	  addAvp(Avp.RESULT_CODE, resultCode);
	}

	public void setRouteRecord(DiameterIdentityAvp routeRecord) {
	  addAvp(Avp.ROUTE_RECORD, routeRecord.toString());
	}

	public void setRouteRecords(DiameterIdentityAvp[] routeRecords) {
		for (DiameterIdentityAvp routeRecord : routeRecords) {
		  addAvp(Avp.ROUTE_RECORD, routeRecord.toString());
		}
	}

	public void setUserName(String userName) {
		addAvp(Avp.USER_NAME, userName);
	}

	public void setVendorSpecificApplicationId(VendorSpecificApplicationIdAvp vsaid)
	{
	  // FIXME: Alexandre: Make it use addAvp(...)
		AvpSet g = setAvpAsGrouped(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, vsaid.getExtensionAvps(), true, false);

		g.addAvp(Avp.VENDOR_ID, (int) vsaid.getVendorId(), true, false);

		if (vsaid.hasAcctApplicationId()) {
			g.addAvp(Avp.ACCT_APPLICATION_ID, (int) vsaid.getAcctApplicationId(), true, false);
		}

		if (vsaid.hasAuthApplicationId()) {
			g.addAvp(Avp.AUTH_APPLICATION_ID, (int) vsaid.getAuthApplicationId(), true, false);
		}
	}

	@Override
	public String toString() {
		DiameterHeader header = this.getHeader();

		String toString = "\r\n" + "+----------------------------------- HEADER ----------------------------------+\r\n" + "| Version................." + header.getVersion() + "\r\n"
				+ "| Message-Length.........." + header.getMessageLength() + "\r\n" + "| Command-Flags..........." + "R[" + header.isRequest() + "] P[" + header.isProxiable() + "] " + "E["
				+ header.isError() + "] T[" + header.isPotentiallyRetransmitted() + "]" + "\r\n" + "| Command-Code............" + this.getHeader().getCommandCode() + "\r\n"
				+ "| Application-Id.........." + this.getHeader().getApplicationId() + "\r\n" + "| Hop-By-Hop Identifier..." + this.getHeader().getHopByHopId() + "\r\n" + "| End-To-End Identifier..."
				+ this.getHeader().getEndToEndId() + "\r\n" + "+------------------------------------ AVPs -----------------------------------+\r\n";

		for (Avp avp : this.getGenericData().getAvps()) {
			toString += printAvp(avp, "");
		}

		toString += "+-----------------------------------------------------------------------------+\r\n";

		return toString;
	}

	// ===== AVP Management =====

	public void addAvp(DiameterAvp avp) {
		addAvpInternal(avp, message.getAvps());
	}

	private void addAvpInternal(DiameterAvp avp, AvpSet set) {
		if (avp.getType() == DiameterAvpType.GROUPED) {
			GroupedAvp gAvp = (GroupedAvp) avp;

			AvpSet groupedAvp = set.addGroupedAvp(gAvp.getCode(), gAvp.getVendorId(), gAvp.getMandatoryRule() != 2, gAvp.getProtectedRule() == 0);

			for (DiameterAvp subAvp : gAvp.getExtensionAvps()) {
				addAvpInternal(subAvp, groupedAvp);
			}
		} else {
			set.addAvp(avp.getCode(), avp.byteArrayValue(), avp.getVendorId(), avp.getMandatoryRule() != 2, avp.getProtectedRule() == 0);
		}
	}

	private DiameterAvp[] getAvpsInternal(AvpSet set) throws Exception {
		List<DiameterAvp> acc = new ArrayList<DiameterAvp>();

		for (Avp a : set) {
			AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(a.getCode(), a.getVendorId());

			if (avpRep == null) {
				log.error("Avp with code: " + a.getCode() + " VendorId: " + a.getVendorId() + " is not listed in dictionary, skipping!");
				continue;
			} else if (avpRep.getType().equals("Grouped")) {
				GroupedAvpImpl gAVP = new GroupedAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw());

				gAVP.setExtensionAvps(getAvpsInternal(a.getGrouped()));

				// This is a grouped AVP... let's make it like that.
				acc.add(gAVP);
			} else {
				acc.add(new DiameterAvpImpl(a.getCode(), a.getVendorId(), a.isMandatory() ? 1 : 0, a.isEncrypted() ? 1 : 0, a.getRaw(), null));
			}
		}

		return acc.toArray(new DiameterAvp[0]);
	}

	private String printAvp(Avp avp, String indent) {
		Object avpValue = null;
		String avpString = "";
		boolean isGrouped = false;

		try {
			String avpType = AvpDictionary.INSTANCE.getAvp(avp.getCode(), avp.getVendorId()).getType();

			if ("Integer32".equals(avpType) || "AppId".equals(avpType)) {
				avpValue = avp.getInteger32();
			} else if ("Unsigned32".equals(avpType) || "VendorId".equals(avpType)) {
				avpValue = avp.getUnsigned32();
			} else if ("Float64".equals(avpType)) {
				avpValue = avp.getFloat64();
			} else if ("Integer64".equals(avpType)) {
				avpValue = avp.getInteger64();
			} else if ("Time".equals(avpType)) {
				avpValue = avp.getTime();
			} else if ("Unsigned64".equals(avpType)) {
				avpValue = avp.getUnsigned64();
			} else if ("Grouped".equals(avpType)) {
				avpValue = "<Grouped>";
				isGrouped = true;
			} else {
				avpValue = avp.getOctetString().replaceAll("\r", "").replaceAll("\n", "");
			}
		} catch (Exception ignore) {
			try {
				avpValue = avp.getOctetString().replaceAll("\r", "").replaceAll("\n", "");
			} catch (AvpDataException e) {
				avpValue = avp.toString();
			}
		}

		avpString += "| " + indent + "AVP: Code[" + avp.getCode() + "] VendorID[" + avp.getVendorId() + "] Value[" + avpValue + "] Flags[M=" + avp.isMandatory() + ";E=" + avp.isEncrypted() + ";V="
				+ avp.isVendorId() + "]\r\n";

		if (isGrouped) {
			try {
				for (Avp subAvp : avp.getGrouped()) {
					avpString += printAvp(subAvp, indent + "  ");
				}
			} catch (AvpDataException e) {
				// Failed to ungroup... ignore then...
			}
		}

		return avpString;
	}

	protected boolean hasAvp(int code) {
		return message.getAvps().getAvp(code) != null;
	}

	protected void reportAvpFetchError(String msg, long code) {
		log.error("Failed to fetch avp, code: " + code + ". Message: " + msg);
	}


}
