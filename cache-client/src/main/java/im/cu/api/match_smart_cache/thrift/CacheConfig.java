/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package im.cu.api.match_smart_cache.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2021-04-12")
public class CacheConfig implements org.apache.thrift.TBase<CacheConfig, CacheConfig._Fields>, java.io.Serializable, Cloneable, Comparable<CacheConfig> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CacheConfig");

  private static final org.apache.thrift.protocol.TField CACHE_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("cacheType", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField DAYS_FIELD_DESC = new org.apache.thrift.protocol.TField("days", org.apache.thrift.protocol.TType.I32, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new CacheConfigStandardSchemeFactory());
    schemes.put(TupleScheme.class, new CacheConfigTupleSchemeFactory());
  }

  /**
   * 
   * @see CacheType
   */
  public CacheType cacheType; // required
  public int days; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see CacheType
     */
    CACHE_TYPE((short)1, "cacheType"),
    DAYS((short)2, "days");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // CACHE_TYPE
          return CACHE_TYPE;
        case 2: // DAYS
          return DAYS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __DAYS_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CACHE_TYPE, new org.apache.thrift.meta_data.FieldMetaData("cacheType", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, CacheType.class)));
    tmpMap.put(_Fields.DAYS, new org.apache.thrift.meta_data.FieldMetaData("days", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CacheConfig.class, metaDataMap);
  }

  public CacheConfig() {
  }

  public CacheConfig(
    CacheType cacheType,
    int days)
  {
    this();
    this.cacheType = cacheType;
    this.days = days;
    setDaysIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public CacheConfig(CacheConfig other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetCacheType()) {
      this.cacheType = other.cacheType;
    }
    this.days = other.days;
  }

  public CacheConfig deepCopy() {
    return new CacheConfig(this);
  }

  @Override
  public void clear() {
    this.cacheType = null;
    setDaysIsSet(false);
    this.days = 0;
  }

  /**
   * 
   * @see CacheType
   */
  public CacheType getCacheType() {
    return this.cacheType;
  }

  /**
   * 
   * @see CacheType
   */
  public CacheConfig setCacheType(CacheType cacheType) {
    this.cacheType = cacheType;
    return this;
  }

  public void unsetCacheType() {
    this.cacheType = null;
  }

  /** Returns true if field cacheType is set (has been assigned a value) and false otherwise */
  public boolean isSetCacheType() {
    return this.cacheType != null;
  }

  public void setCacheTypeIsSet(boolean value) {
    if (!value) {
      this.cacheType = null;
    }
  }

  public int getDays() {
    return this.days;
  }

  public CacheConfig setDays(int days) {
    this.days = days;
    setDaysIsSet(true);
    return this;
  }

  public void unsetDays() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DAYS_ISSET_ID);
  }

  /** Returns true if field days is set (has been assigned a value) and false otherwise */
  public boolean isSetDays() {
    return EncodingUtils.testBit(__isset_bitfield, __DAYS_ISSET_ID);
  }

  public void setDaysIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DAYS_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CACHE_TYPE:
      if (value == null) {
        unsetCacheType();
      } else {
        setCacheType((CacheType)value);
      }
      break;

    case DAYS:
      if (value == null) {
        unsetDays();
      } else {
        setDays((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CACHE_TYPE:
      return getCacheType();

    case DAYS:
      return getDays();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CACHE_TYPE:
      return isSetCacheType();
    case DAYS:
      return isSetDays();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CacheConfig)
      return this.equals((CacheConfig)that);
    return false;
  }

  public boolean equals(CacheConfig that) {
    if (that == null)
      return false;

    boolean this_present_cacheType = true && this.isSetCacheType();
    boolean that_present_cacheType = true && that.isSetCacheType();
    if (this_present_cacheType || that_present_cacheType) {
      if (!(this_present_cacheType && that_present_cacheType))
        return false;
      if (!this.cacheType.equals(that.cacheType))
        return false;
    }

    boolean this_present_days = true;
    boolean that_present_days = true;
    if (this_present_days || that_present_days) {
      if (!(this_present_days && that_present_days))
        return false;
      if (this.days != that.days)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_cacheType = true && (isSetCacheType());
    list.add(present_cacheType);
    if (present_cacheType)
      list.add(cacheType.getValue());

    boolean present_days = true;
    list.add(present_days);
    if (present_days)
      list.add(days);

    return list.hashCode();
  }

  @Override
  public int compareTo(CacheConfig other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetCacheType()).compareTo(other.isSetCacheType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCacheType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cacheType, other.cacheType);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDays()).compareTo(other.isSetDays());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDays()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.days, other.days);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CacheConfig(");
    boolean first = true;

    sb.append("cacheType:");
    if (this.cacheType == null) {
      sb.append("null");
    } else {
      sb.append(this.cacheType);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("days:");
    sb.append(this.days);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class CacheConfigStandardSchemeFactory implements SchemeFactory {
    public CacheConfigStandardScheme getScheme() {
      return new CacheConfigStandardScheme();
    }
  }

  private static class CacheConfigStandardScheme extends StandardScheme<CacheConfig> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, CacheConfig struct) throws TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CACHE_TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.cacheType = CacheType.findByValue(iprot.readI32());
              struct.setCacheTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DAYS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.days = iprot.readI32();
              struct.setDaysIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, CacheConfig struct) throws TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.cacheType != null) {
        oprot.writeFieldBegin(CACHE_TYPE_FIELD_DESC);
        oprot.writeI32(struct.cacheType.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(DAYS_FIELD_DESC);
      oprot.writeI32(struct.days);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class CacheConfigTupleSchemeFactory implements SchemeFactory {
    public CacheConfigTupleScheme getScheme() {
      return new CacheConfigTupleScheme();
    }
  }

  private static class CacheConfigTupleScheme extends TupleScheme<CacheConfig> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, CacheConfig struct) throws TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetCacheType()) {
        optionals.set(0);
      }
      if (struct.isSetDays()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetCacheType()) {
        oprot.writeI32(struct.cacheType.getValue());
      }
      if (struct.isSetDays()) {
        oprot.writeI32(struct.days);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, CacheConfig struct) throws TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.cacheType = CacheType.findByValue(iprot.readI32());
        struct.setCacheTypeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.days = iprot.readI32();
        struct.setDaysIsSet(true);
      }
    }
  }

}

