package de.thoffbauer.signal4j.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.thoffbauer.signal4j.store.serialize.GroupIdDeserializer;
import de.thoffbauer.signal4j.store.serialize.GroupIdKeyDeserializer;
import de.thoffbauer.signal4j.store.serialize.GroupIdKeySerializer;
import de.thoffbauer.signal4j.store.serialize.GroupIdSerializer;
import de.thoffbauer.signal4j.store.serialize.IdentityKeyDeserializer;
import de.thoffbauer.signal4j.store.serialize.IdentityKeyPairDeserializer;
import de.thoffbauer.signal4j.store.serialize.IdentityKeyPairSerializer;
import de.thoffbauer.signal4j.store.serialize.IdentityKeySerializer;
import de.thoffbauer.signal4j.store.serialize.PreKeyRecordDeserializer;
import de.thoffbauer.signal4j.store.serialize.PreKeyRecordSerializer;
import de.thoffbauer.signal4j.store.serialize.SessionRecordDeserializer;
import de.thoffbauer.signal4j.store.serialize.SessionRecordSerializer;
import de.thoffbauer.signal4j.store.serialize.SignalProtocolAddressDeserializer;
import de.thoffbauer.signal4j.store.serialize.SignalProtocolAddressSerializer;
import de.thoffbauer.signal4j.store.serialize.SignedPreKeyRecordDeserializer;
import de.thoffbauer.signal4j.store.serialize.SignedPreKeyRecordSerializer;

public class JsonSignalStore extends SignalStore {

	@JsonProperty
	private IdentityKeyPair identityKeyPair;
	@JsonProperty
	private int registrationId;
	@JsonProperty
	private String password;
	@JsonProperty
	private String signalingKey;
	@JsonProperty
	private String phoneNumber;
	@JsonProperty
	private String userAgent;
	@JsonProperty
	private String url;
	@JsonProperty
	private int deviceId;
	@JsonProperty
	private PreKeyRecord lastResortPreKey;
	@JsonProperty
	private int nextPreKeyId;
	@JsonProperty
	private int nextSignedPreKeyId;

	@JsonProperty
	private HashMap<SignalProtocolAddress, IdentityKey> identities = new HashMap<>();
	@JsonProperty
	private HashMap<Integer, PreKeyRecord> preKeys = new HashMap<>();
	@JsonProperty
	private HashMap<Integer, SignedPreKeyRecord> signedPreKeys = new HashMap<>();
	@JsonProperty
	private HashMap<SignalProtocolAddress, SessionRecord> sessions = new HashMap<>();
	
	@JsonProperty("data")
	private JsonDataStore dataStore = new JsonDataStore();
	
	public static JsonSignalStore load(File file) throws IOException {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(IdentityKeyPair.class, new IdentityKeyPairDeserializer());
		module.addDeserializer(IdentityKey.class, new IdentityKeyDeserializer());
		module.addDeserializer(PreKeyRecord.class, new PreKeyRecordDeserializer());
		module.addDeserializer(SignedPreKeyRecord.class, new SignedPreKeyRecordDeserializer());
		module.addDeserializer(SessionRecord.class, new SessionRecordDeserializer());
		module.addDeserializer(GroupId.class, new GroupIdDeserializer());
		module.addKeyDeserializer(SignalProtocolAddress.class, new SignalProtocolAddressDeserializer());
		module.addKeyDeserializer(GroupId.class, new GroupIdKeyDeserializer());
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(module);
		return mapper.readValue(file, JsonSignalStore.class);
	}
	
	public void save(File file) throws IOException {
		SimpleModule module = new SimpleModule();
		module.addSerializer(IdentityKeyPair.class, new IdentityKeyPairSerializer());
		module.addSerializer(IdentityKey.class, new IdentityKeySerializer());
		module.addSerializer(PreKeyRecord.class, new PreKeyRecordSerializer());
		module.addSerializer(SignedPreKeyRecord.class, new SignedPreKeyRecordSerializer());
		module.addSerializer(SessionRecord.class, new SessionRecordSerializer());
		module.addSerializer(GroupId.class, new GroupIdSerializer());
		module.addKeySerializer(SignalProtocolAddress.class, new SignalProtocolAddressSerializer());
		module.addKeySerializer(GroupId.class, new GroupIdKeySerializer());
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(file, this);
	}

	@Override
	public IdentityKey getIdentity(SignalProtocolAddress address){
		return identities.get(address);
	}

	@Override
	public DataStore getDataStore() {
		return dataStore;
	}
	
	@Override
	@JsonIgnore
	public IdentityKeyPair getIdentityKeyPair() {
		return identityKeyPair;
	}

	@JsonIgnore
	public void setIdentityKeyPair(IdentityKeyPair identityKeyPair) {
		this.identityKeyPair = identityKeyPair;
	}

	@Override
	@JsonIgnore
	public int getLocalRegistrationId() {
		return registrationId;
	}

	@Override
	public boolean saveIdentity(SignalProtocolAddress address,IdentityKey identityKey){
		identities.put(address,identityKey);
		return true;
	}

	@Override
	@JsonIgnore
	public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
		return preKeys.get(preKeyId);
	}

	@Override
	@JsonIgnore
	public void storePreKey(int preKeyId, PreKeyRecord record) {
		preKeys.put(preKeyId, record);
	}

	@Override
	@JsonIgnore
	public boolean containsPreKey(int preKeyId) {
		return preKeys.containsKey(preKeyId);
	}

	@Override
	@JsonIgnore
	public void removePreKey(int preKeyId) {
		preKeys.remove(preKeyId);
	}

	@Override
	@JsonIgnore
	public SessionRecord getSession(SignalProtocolAddress address) {
		return sessions.get(address);
	}
	
	@Override
	@JsonIgnore
	public Iterable<Entry<SignalProtocolAddress, SessionRecord>> getSessions() {
		return sessions.entrySet();
	}

	@Override
	@JsonIgnore
	public void storeSession(SignalProtocolAddress address, SessionRecord record) {
		sessions.put(address, record);
	}

	@Override
	@JsonIgnore
	public boolean containsSession(SignalProtocolAddress address) {
		return sessions.containsKey(address);
	}

	@Override
	@JsonIgnore
	public void deleteSession(SignalProtocolAddress address) {
		sessions.remove(address);
	}

	@Override
	@JsonIgnore
	public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
		return signedPreKeys.get(signedPreKeyId);
	}

	@Override
	@JsonIgnore
	public List<SignedPreKeyRecord> loadSignedPreKeys() {
		return new ArrayList<>(signedPreKeys.values());
	}

	@Override
	@JsonIgnore
	public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
		signedPreKeys.put(signedPreKeyId, record);
	}

	@Override
	@JsonIgnore
	public boolean containsSignedPreKey(int signedPreKeyId) {
		return signedPreKeys.containsKey(signedPreKeyId);
	}

	@Override
	@JsonIgnore
	public void removeSignedPreKey(int signedPreKeyId) {
		signedPreKeys.remove(signedPreKeyId);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSignalingKey() {
		return signalingKey;
	}

	public void setSignalingKey(String signalingKey) {
		this.signalingKey = signalingKey;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public void setLocalRegistrationId(int registrationId) {
		this.registrationId = registrationId;
	}

	public PreKeyRecord getLastResortPreKey() {
		return lastResortPreKey;
	}

	public void setLastResortPreKey(PreKeyRecord lastResortPreKey) {
		this.lastResortPreKey = lastResortPreKey;
	}

	public int getNextPreKeyId() {
		return nextPreKeyId;
	}

	public void setNextPreKeyId(int nextPreKeyId) {
		this.nextPreKeyId = nextPreKeyId;
	}

	public int getNextSignedPreKeyId() {
		return nextSignedPreKeyId;
	}

	public void setNextSignedPreKeyId(int nextSignedPreKeyId) {
		this.nextSignedPreKeyId = nextSignedPreKeyId;
	}

}
