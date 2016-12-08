package me.krstic.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Digital {

	@Id
	@GeneratedValue
	private Long id;
	private String clientId;
	private String clientSystem;
	private String operaterId;
	private String operaterUsername;
	private String operaterMachine;
	private String dataType;
	private String data;
	private String status;
	private String tsaResponse;
	private String guid;
//	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp startDate;
//	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp modifyDate;
//	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp endDate;
	
	public Digital() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSystem() {
		return clientSystem;
	}

	public void setClientSystem(String clientSystem) {
		this.clientSystem = clientSystem;
	}

	public String getOperaterId() {
		return operaterId;
	}

	public void setOperaterId(String operaterId) {
		this.operaterId = operaterId;
	}

	public String getOperaterUsername() {
		return operaterUsername;
	}

	public void setOperaterUsername(String operaterUsername) {
		this.operaterUsername = operaterUsername;
	}

	public String getOperaterMachine() {
		return operaterMachine;
	}

	public void setOperaterMachine(String operaterMachine) {
		this.operaterMachine = operaterMachine;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTsaResponse() {
		return tsaResponse;
	}

	public void setTsaResponse(String tsaResponse) {
		this.tsaResponse = tsaResponse;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Timestamp modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "Digital [id=" + id + ", clientId=" + clientId + ", clientSystem=" + clientSystem + ", operaterId="
				+ operaterId + ", operaterUsername=" + operaterUsername + ", operaterMachine=" + operaterMachine
				+ ", dataType=" + dataType + ", data=" + data + ", status=" + status + ", tsaResponse=" + tsaResponse
				+ ", guid=" + guid + ", startDate=" + startDate + ", modifyDate=" + modifyDate + ", endDate=" + endDate
				+ "]";
	}
}
