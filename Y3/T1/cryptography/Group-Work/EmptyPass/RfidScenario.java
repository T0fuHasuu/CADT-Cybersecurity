package com.regula.documentreader.api.params;

import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.enums.eVisualFieldType;
import com.regula.documentreader.api.internal.utils.JsonUtil;
import com.regula.documentreader.api.params.rfid.dg.DTCDataGroup;
import com.regula.documentreader.api.params.rfid.dg.EDLDataGroups;
import com.regula.documentreader.api.params.rfid.dg.EIDDataGroups;
import com.regula.documentreader.api.params.rfid.dg.EPassportDataGroups;
import com.regula.documentreader.api.results.DocumentReaderResults;
import org.json.JSONObject;

public class RfidScenario {
    private boolean applyAmendments = true;
    private final int coreDefaultReadingBufferSize = 0;
    private int defaultReadingBufferSize = 0;
    private int mAuthProcType = 2;
    private boolean mAuthorizedCANAllowed;
    private boolean mAuthorizedInstallCert;
    private boolean mAuthorizedInstallQCert;
    private boolean mAuthorizedPINManagement;
    private boolean mAuthorizedPrivilegedTerminal;
    private boolean mAuthorizedRestrictedIdentification;
    private boolean mAuthorizedSTQSignature;
    private boolean mAuthorizedSTSignature;
    private boolean mAuthorizedVerifyAge;
    private boolean mAuthorizedVerifyCommunityID;
    private boolean mAuthorizedWriteDG17;
    private boolean mAuthorizedWriteDG18;
    private boolean mAuthorizedWriteDG19;
    private boolean mAuthorizedWriteDG20;
    private boolean mAuthorizedWriteDG21;
    private boolean mAutoSettings = true;
    private boolean mAuxVerificationCommunityID;
    private boolean mAuxVerificationDateOfBirth;
    private int mBaseSMProcedure = 2;
    private String mCardAccess;
    private DTCDataGroup mDTCDataGroup = DTCDataGroup.defaultSettingsInstance();
    private EDLDataGroups mEDLDataGroups = EDLDataGroups.defaultSettingsInstance();
    private EIDDataGroups mEIDDataGroups = EIDDataGroups.defaultSettingsInstance();
    private EPassportDataGroups mEPassportDataGroups = EPassportDataGroups.defaultSettingsInstance();
    private String mESignPINDefault = "";
    private String mESignPINNewValue = "";
    private String mMrz = "";
    private boolean mOnlineTA;
    private int mOnlineTAToSignDataType;
    private int mPacePasswordType = 1;
    private boolean mPaceStaticBinding;
    private boolean mPassiveAuth = true;
    private String mPassword = "";
    private boolean mPkdDSCertPriority;
    private String mPkdEAC = "";
    private String mPkdPA = "";
    private boolean mPkdUseExternalCSCA;
    private int mProfilerType = 2;
    private boolean mReadEDL = true;
    private boolean mReadEID;
    private boolean mReadEPassport = true;
    private int mReadingBuffer;
    private int mSignManagementAction = 0;
    private boolean mSkipAA;
    private boolean mStrictProcessing;
    private int mTerminalType = 1;
    private boolean mTrustedPKD = true;
    private boolean mUniversalAccessRights;
    private boolean mUseSFI;
    private boolean mWriteEid;
    private Boolean mrzStrictCheck;
    private boolean proceedReadingAlways = false;
    private boolean readDTC = true;
    private boolean readSAM;

    public String toCoreJson(DocumentReaderResults documentReaderResults) {
        RfidScenario fromJson;
        if (documentReaderResults == null || !isAutoSettings()) {
            JSONObject coreJsonObject = toCoreJsonObject();
            if (coreJsonObject != null) {
                return coreJsonObject.toString();
            }
            return null;
        }
        JSONObject jsonObject = toJsonObject();
        if (jsonObject == null || (fromJson = new RfidScenario().fromJson(jsonObject.toString())) == null) {
            return null;
        }
        String textFieldValueByType = documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS_ICAO_RFID);
        if (textFieldValueByType == null || textFieldValueByType.isEmpty()) {
            String textFieldValueByType2 = documentReaderResults.getTextFieldValueByType(51);
            if (textFieldValueByType2 == null || textFieldValueByType2.isEmpty()) {
                String textFieldValueByType3 = documentReaderResults.getTextFieldValueByType(159);
                if (textFieldValueByType3 != null && !textFieldValueByType3.isEmpty()) {
                    fromJson.setPassword(textFieldValueByType3);
                    fromJson.setPacePasswordType(2);
                    fromJson.setBaseSMProcedure(2);
                }
            } else {
                fromJson.setMrz(documentReaderResults.getTextFieldValueByType(51).replace("\n", ""));
                fromJson.setPacePasswordType(1);
            }
        } else {
            fromJson.setMrz(documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS_ICAO_RFID).replace("\n", ""));
            fromJson.setPacePasswordType(1);
        }
        if (fromJson.getMrz().length() == 30 && fromJson.getMrz().charAt(0) == 'D') {
            fromJson.setReadEDL(true);
            fromJson.setReadEPassport(false);
        } else {
            fromJson.setReadEDL(false);
            fromJson.setReadEPassport(true);
        }
        JSONObject coreJsonObject2 = fromJson.toCoreJsonObject();
        if (coreJsonObject2 != null) {
            return coreJsonObject2.toString();
        }
        return null;
    }

    public String toJson() {
        JSONObject jsonObject = toJsonObject();
        if (jsonObject != null) {
            return jsonObject.toString();
        }
        return null;
    }

    private JSONObject toCoreJsonObject() {
        JSONObject jsonObject = toJsonObject();
        if (jsonObject == null) {
            return null;
        }
        if (jsonObject.has("Read_ePassport") && !jsonObject.optBoolean("Read_ePassport")) {
            jsonObject.remove("ePassport");
        }
        if (jsonObject.has("Read_eID") && !jsonObject.optBoolean("Read_eID")) {
            jsonObject.remove("eID");
        }
        if (jsonObject.has("Read_eDL") && !jsonObject.optBoolean("Read_eDL")) {
            jsonObject.remove("eDL");
        }
        if (jsonObject.has("Read_DTC") && !jsonObject.optBoolean("Read_DTC")) {
            jsonObject.remove("eDTC_PC");
        }
        return jsonObject;
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("AuthProcType", getAuthProcType());
            jSONObject.put("AuxVerification_CommunityID", isAuxVerificationCommunityID());
            jSONObject.put("AuxVerification_DateOfBirth", isAuxVerificationDateOfBirth());
            jSONObject.put("BaseSMProcedure", getBaseSMProcedure());
            jSONObject.put("OnlineTA", isOnlineTA());
            jSONObject.put("OnlineTAToSignDataType", getOnlineTAToSignDataType());
            jSONObject.put("PACE_StaticBinding", isPaceStaticBinding());
            jSONObject.put("PKD_DSCert_Priority", isPkdDSCertPriority());
            jSONObject.put("PKD_EAC", getPkdEAC());
            jSONObject.put("PKD_PA", getPkdPA());
            jSONObject.put("PKD_UseExternalCSCA", isPkdUseExternalCSCA());
            jSONObject.put("PassiveAuth", isPassiveAuth());
            jSONObject.put("Perform_RestrictedIdentification", isAuthorizedRestrictedIdentification());
            jSONObject.put("ProfilerType", getProfilerType());
            jSONObject.put("ReadingBuffer", getReadingBuffer());
            jSONObject.put("SkipAA", isSkipAA());
            jSONObject.put("StrictProcessing", isStrictProcessing());
            jSONObject.put("TerminalType", getTerminalType());
            jSONObject.put("TrustedPKD", isTrustedPKD());
            jSONObject.put("UniversalAccessRights", isUniversalAccessRights());
            jSONObject.put("Use_SFI", isUseSFI());
            jSONObject.put("Write_eID", isWriteEid());
            jSONObject.put("SignManagementAction", getSignManagementAction());
            jSONObject.put("eSignPIN_Default", geteSignPINDefault());
            jSONObject.put("eSignPIN_NewValue", geteSignPINNewValue());
            jSONObject.put("Authorized_ST_Signature", isAuthorizedSTSignature());
            jSONObject.put("Authorized_ST_QSignature", isAuthorizedSTQSignature());
            jSONObject.put("Authorized_Write_DG17", isAuthorizedWriteDG17());
            jSONObject.put("Authorized_Write_DG18", isAuthorizedWriteDG18());
            jSONObject.put("Authorized_Write_DG19", isAuthorizedWriteDG19());
            jSONObject.put("Authorized_Write_DG20", isAuthorizedWriteDG20());
            jSONObject.put("Authorized_Write_DG21", isAuthorizedWriteDG21());
            jSONObject.put("Authorized_Verify_Age", isAuthorizedVerifyAge());
            jSONObject.put("Authorized_Verify_CommunityID", isAuthorizedVerifyCommunityID());
            jSONObject.put("Authorized_PrivilegedTerminal", isAuthorizedPrivilegedTerminal());
            jSONObject.put("Authorized_CAN_Allowed", isAuthorizedCANAllowed());
            jSONObject.put("Authorized_PIN_Managment", isAuthorizedPINManagment());
            jSONObject.put("Authorized_Install_Cert", isAuthorizedInstallCert());
            jSONObject.put("Authorized_Install_QCert", isAuthorizedInstallQCert());
            jSONObject.put("Read_ePassport", isReadEPassport());
            jSONObject.put("ePassport", new JSONObject(ePassportDataGroups().toJson()));
            jSONObject.put("Read_DTC", isReadDTC());
            jSONObject.put("eDTC_PC", new JSONObject(DTCDataGroup().toJson()));
            jSONObject.put("Read_eID", isReadEID());
            jSONObject.put("eID", new JSONObject(eIDDataGroups().toJson()));
            jSONObject.put("Read_eDL", isReadEDL());
            jSONObject.put("eDL", new JSONObject(eDLDataGroups().toJson()));
            jSONObject.put("PACEPasswordType", getPacePasswordType());
            if (getPacePasswordType() == 1) {
                jSONObject.put("MRZ", getMrz());
            } else {
                jSONObject.put("Password", getPassword());
            }
            if (getCardAccess() != null) {
                jSONObject.put("CardAccess", getCardAccess());
            }
            jSONObject.put("applyAmendments", this.applyAmendments);
            jSONObject.put("Read_SAM", isReadSAM());
            jSONObject.put("DefaultReadingBufferSize", getDefaultReadingBufferSize());
            jSONObject.put("proceedReadingAlways", this.proceedReadingAlways);
            JsonUtil.safePutObjectValue(jSONObject, "mrzStrictCheck", this.mrzStrictCheck);
            return jSONObject;
        } catch (Exception e) {
            DocumentReader.Instance().LOG.d(e);
            return null;
        }
    }

    public RfidScenario fromJson(String str) {
        String str2 = null;
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            setPaceStaticBinding(jSONObject.optBoolean("PACE_StaticBinding"));
            setSignManagementAction(jSONObject.optInt("SignManagementAction"));
            setReadingBuffer(jSONObject.optInt("ReadingBuffer"));
            setOnlineTAToSignDataType(jSONObject.optInt("OnlineTAToSignDataType"));
            setOnlineTA(jSONObject.optBoolean("OnlineTA"));
            setWriteEid(jSONObject.optBoolean("Write_eID"));
            setProfilerType(jSONObject.optInt("ProfilerType", 1));
            setAuthProcType(jSONObject.optInt("AuthProcType", 1));
            setBaseSMProcedure(jSONObject.optInt("BaseSMProcedure", 1));
            setPacePasswordType(jSONObject.optInt("PACEPasswordType", 1));
            setTerminalType(jSONObject.optInt("TerminalType", 1));
            setUniversalAccessRights(jSONObject.optBoolean("UniversalAccessRights"));
            setAuthorizedRestrictedIdentification(jSONObject.optBoolean("Perform_RestrictedIdentification"));
            setAuxVerificationCommunityID(jSONObject.optBoolean("AuxVerification_CommunityID"));
            setAuxVerificationDateOfBirth(jSONObject.optBoolean("AuxVerification_DateOfBirth"));
            setSkipAA(jSONObject.optBoolean("SkipAA"));
            setStrictProcessing(jSONObject.optBoolean("StrictProcessing"));
            setPkdDSCertPriority(jSONObject.optBoolean("PKD_DSCert_Priority"));
            setPkdUseExternalCSCA(jSONObject.optBoolean("PKD_UseExternalCSCA"));
            setTrustedPKD(jSONObject.optBoolean("TrustedPKD", true));
            setPassiveAuth(jSONObject.optBoolean("PassiveAuth", true));
            setPassword(jSONObject.optString("Password"));
            setUseSFI(jSONObject.optBoolean("Use_SFI"));
            setPkdPA(jSONObject.optString("PKD_PA"));
            setPkdEAC(jSONObject.optString("PKD_EAC"));
            setReadEPassport(jSONObject.optBoolean("Read_ePassport", true));
            setReadDTC(jSONObject.optBoolean("Read_DTC", true));
            setReadEID(jSONObject.optBoolean("Read_eID"));
            setReadEDL(jSONObject.optBoolean("Read_eDL", true));
            setMrz(jSONObject.optString("MRZ"));
            if (jSONObject.has("CardAccess")) {
                str2 = jSONObject.optString("CardAccess", (String) null);
            }
            setCardAccess(str2);
            seteSignPINDefault(jSONObject.optString("eSignPIN_Default"));
            seteSignPINNewValue(jSONObject.optString("eSignPIN_NewValue"));
            setAuthorizedSTSignature(jSONObject.optBoolean("Authorized_ST_Signature"));
            setAuthorizedSTQSignature(jSONObject.optBoolean("Authorized_ST_QSignature"));
            setAuthorizedWriteDG17(jSONObject.optBoolean("Authorized_Write_DG17"));
            setAuthorizedWriteDG18(jSONObject.optBoolean("Authorized_Write_DG18"));
            setAuthorizedWriteDG19(jSONObject.optBoolean("Authorized_Write_DG19"));
            setAuthorizedWriteDG20(jSONObject.optBoolean("Authorized_Write_DG20"));
            setAuthorizedWriteDG21(jSONObject.optBoolean("Authorized_Write_DG21"));
            setAuthorizedVerifyAge(jSONObject.optBoolean("Authorized_Verify_Age"));
            setAuthorizedVerifyCommunityID(jSONObject.optBoolean("Authorized_Verify_CommunityID"));
            setAuthorizedPrivilegedTerminal(jSONObject.optBoolean("Authorized_PrivilegedTerminal"));
            setAuthorizedCANAllowed(jSONObject.optBoolean("Authorized_CAN_Allowed"));
            setAuthorizedPINManagment(jSONObject.optBoolean("Authorized_PIN_Managment"));
            setAuthorizedInstallCert(jSONObject.optBoolean("Authorized_Install_Cert"));
            setAuthorizedInstallQCert(jSONObject.optBoolean("Authorized_Install_QCert"));
            setApplyAmendments(jSONObject.optBoolean("applyAmendments", true));
            setReadSAM(jSONObject.optBoolean("Read_SAM", false));
            setDefaultReadingBufferSize(jSONObject.optInt("DefaultReadingBufferSize", 0));
            setProceedReadingAlways(jSONObject.optBoolean("proceedReadingAlways"));
            if (jSONObject.has("mrzStrictCheck")) {
                setMrzStrictCheck(jSONObject.optBoolean("mrzStrictCheck"));
            }
            this.mEPassportDataGroups = EPassportDataGroups.fromJson(jSONObject.optString("ePassport"));
            this.mDTCDataGroup = DTCDataGroup.fromJson(jSONObject.optString("eDTC_PC"));
            this.mEIDDataGroups = EIDDataGroups.fromJson(jSONObject.optString("eID"));
            this.mEDLDataGroups = EDLDataGroups.fromJson(jSONObject.optString("eDL"));
        } catch (Exception e) {
            DocumentReader.Instance().LOG.d(e);
        }
        return this;
    }

    public int getSignManagementAction() {
        return this.mSignManagementAction;
    }

    public void setSignManagementAction(int i) {
        this.mSignManagementAction = i;
    }

    public int getReadingBuffer() {
        return this.mReadingBuffer;
    }

    public void setReadingBuffer(int i) {
        this.mReadingBuffer = i;
    }

    public int getOnlineTAToSignDataType() {
        return this.mOnlineTAToSignDataType;
    }

    public void setOnlineTAToSignDataType(int i) {
        this.mOnlineTAToSignDataType = i;
    }

    public boolean isOnlineTA() {
        return this.mOnlineTA;
    }

    public void setOnlineTA(boolean z) {
        this.mOnlineTA = z;
    }

    public boolean isWriteEid() {
        return this.mWriteEid;
    }

    public void setWriteEid(boolean z) {
        this.mWriteEid = z;
    }

    public int getProfilerType() {
        return this.mProfilerType;
    }

    public void setProfilerType(int i) {
        this.mProfilerType = i;
    }

    public int getAuthProcType() {
        return this.mAuthProcType;
    }

    public void setAuthProcType(int i) {
        this.mAuthProcType = i;
    }

    public int getBaseSMProcedure() {
        return this.mBaseSMProcedure;
    }

    public void setBaseSMProcedure(int i) {
        this.mBaseSMProcedure = i;
    }

    public int getPacePasswordType() {
        return this.mPacePasswordType;
    }

    public void setPacePasswordType(int i) {
        this.mPacePasswordType = i;
    }

    public int getTerminalType() {
        return this.mTerminalType;
    }

    public void setTerminalType(int i) {
        this.mTerminalType = i;
    }

    public boolean isUniversalAccessRights() {
        return this.mUniversalAccessRights;
    }

    public void setUniversalAccessRights(boolean z) {
        this.mUniversalAccessRights = z;
    }

    public boolean isAuthorizedRestrictedIdentification() {
        return this.mAuthorizedRestrictedIdentification;
    }

    public void setAuthorizedRestrictedIdentification(boolean z) {
        this.mAuthorizedRestrictedIdentification = z;
    }

    public boolean isAuxVerificationCommunityID() {
        return this.mAuxVerificationCommunityID;
    }

    public void setAuxVerificationCommunityID(boolean z) {
        this.mAuxVerificationCommunityID = z;
    }

    public boolean isAuxVerificationDateOfBirth() {
        return this.mAuxVerificationDateOfBirth;
    }

    public void setAuxVerificationDateOfBirth(boolean z) {
        this.mAuxVerificationDateOfBirth = z;
    }

    public boolean isSkipAA() {
        return this.mSkipAA;
    }

    public void setSkipAA(boolean z) {
        this.mSkipAA = z;
    }

    public boolean isStrictProcessing() {
        return this.mStrictProcessing;
    }

    public void setStrictProcessing(boolean z) {
        this.mStrictProcessing = z;
    }

    public boolean isPkdDSCertPriority() {
        return this.mPkdDSCertPriority;
    }

    public void setPkdDSCertPriority(boolean z) {
        this.mPkdDSCertPriority = z;
    }

    public boolean isPkdUseExternalCSCA() {
        return this.mPkdUseExternalCSCA;
    }

    public void setPkdUseExternalCSCA(boolean z) {
        this.mPkdUseExternalCSCA = z;
    }

    public boolean isTrustedPKD() {
        return this.mTrustedPKD;
    }

    public void setTrustedPKD(boolean z) {
        this.mTrustedPKD = z;
    }

    public boolean isPassiveAuth() {
        return this.mPassiveAuth;
    }

    public void setPassiveAuth(boolean z) {
        this.mPassiveAuth = z;
    }

    public boolean isPaceStaticBinding() {
        return this.mPaceStaticBinding;
    }

    public void setPaceStaticBinding(boolean z) {
        this.mPaceStaticBinding = z;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public void setPassword(String str) {
        this.mPassword = str;
    }

    public boolean isUseSFI() {
        return this.mUseSFI;
    }

    public void setUseSFI(boolean z) {
        this.mUseSFI = z;
    }

    public String getPkdPA() {
        return this.mPkdPA;
    }

    public void setPkdPA(String str) {
        this.mPkdPA = str;
    }

    public String getPkdEAC() {
        return this.mPkdEAC;
    }

    public void setPkdEAC(String str) {
        this.mPkdEAC = str;
    }

    public boolean isReadEPassport() {
        return this.mReadEPassport;
    }

    public void setReadEPassport(boolean z) {
        this.mReadEPassport = z;
    }

    public boolean isReadEID() {
        return this.mReadEID;
    }

    public void setReadEID(boolean z) {
        this.mReadEID = z;
    }

    public boolean isReadEDL() {
        return this.mReadEDL;
    }

    public void setReadEDL(boolean z) {
        this.mReadEDL = z;
    }

    public String getMrz() {
        return this.mMrz;
    }

    public void setMrz(String str) {
        this.mMrz = str;
    }

    public String getCardAccess() {
        return this.mCardAccess;
    }

    public void setCardAccess(String str) {
        this.mCardAccess = str;
    }

    public String geteSignPINDefault() {
        return this.mESignPINDefault;
    }

    public void seteSignPINDefault(String str) {
        this.mESignPINDefault = str;
    }

    public String geteSignPINNewValue() {
        return this.mESignPINNewValue;
    }

    public void seteSignPINNewValue(String str) {
        this.mESignPINNewValue = str;
    }

    public boolean isAuthorizedSTSignature() {
        return this.mAuthorizedSTSignature;
    }

    public void setAuthorizedSTSignature(boolean z) {
        this.mAuthorizedSTSignature = z;
    }

    public boolean isAuthorizedSTQSignature() {
        return this.mAuthorizedSTQSignature;
    }

    public void setAuthorizedSTQSignature(boolean z) {
        this.mAuthorizedSTQSignature = z;
    }

    public boolean isAuthorizedWriteDG17() {
        return this.mAuthorizedWriteDG17;
    }

    public void setAuthorizedWriteDG17(boolean z) {
        this.mAuthorizedWriteDG17 = z;
    }

    public boolean isAuthorizedWriteDG18() {
        return this.mAuthorizedWriteDG18;
    }

    public void setAuthorizedWriteDG18(boolean z) {
        this.mAuthorizedWriteDG18 = z;
    }

    public boolean isAuthorizedWriteDG19() {
        return this.mAuthorizedWriteDG19;
    }

    public void setAuthorizedWriteDG19(boolean z) {
        this.mAuthorizedWriteDG19 = z;
    }

    public boolean isAuthorizedWriteDG20() {
        return this.mAuthorizedWriteDG20;
    }

    public void setAuthorizedWriteDG20(boolean z) {
        this.mAuthorizedWriteDG20 = z;
    }

    public boolean isAuthorizedWriteDG21() {
        return this.mAuthorizedWriteDG21;
    }

    public void setAuthorizedWriteDG21(boolean z) {
        this.mAuthorizedWriteDG21 = z;
    }

    public boolean isAuthorizedVerifyAge() {
        return this.mAuthorizedVerifyAge;
    }

    public void setAuthorizedVerifyAge(boolean z) {
        this.mAuthorizedVerifyAge = z;
    }

    public boolean isAuthorizedVerifyCommunityID() {
        return this.mAuthorizedVerifyCommunityID;
    }

    public void setAuthorizedVerifyCommunityID(boolean z) {
        this.mAuthorizedVerifyCommunityID = z;
    }

    public boolean isAuthorizedPrivilegedTerminal() {
        return this.mAuthorizedPrivilegedTerminal;
    }

    public void setAuthorizedPrivilegedTerminal(boolean z) {
        this.mAuthorizedPrivilegedTerminal = z;
    }

    public boolean isAuthorizedCANAllowed() {
        return this.mAuthorizedCANAllowed;
    }

    public void setAuthorizedCANAllowed(boolean z) {
        this.mAuthorizedCANAllowed = z;
    }

    public boolean isAuthorizedPINManagment() {
        return this.mAuthorizedPINManagement;
    }

    public void setAuthorizedPINManagment(boolean z) {
        this.mAuthorizedPINManagement = z;
    }

    public boolean isAuthorizedInstallCert() {
        return this.mAuthorizedInstallCert;
    }

    public void setAuthorizedInstallCert(boolean z) {
        this.mAuthorizedInstallCert = z;
    }

    public boolean isAuthorizedInstallQCert() {
        return this.mAuthorizedInstallQCert;
    }

    public void setAuthorizedInstallQCert(boolean z) {
        this.mAuthorizedInstallQCert = z;
    }

    public EPassportDataGroups ePassportDataGroups() {
        return this.mEPassportDataGroups;
    }

    public EIDDataGroups eIDDataGroups() {
        return this.mEIDDataGroups;
    }

    public EDLDataGroups eDLDataGroups() {
        return this.mEDLDataGroups;
    }

    public DTCDataGroup DTCDataGroup() {
        return this.mDTCDataGroup;
    }

    public void setAutoSettings(boolean z) {
        this.mAutoSettings = z;
    }

    public boolean isAutoSettings() {
        return this.mAutoSettings;
    }

    public void setApplyAmendments(boolean z) {
        this.applyAmendments = z;
    }

    public boolean isApplyAmendments() {
        return this.applyAmendments;
    }

    public void setReadSAM(boolean z) {
        this.readSAM = z;
    }

    public boolean isReadSAM() {
        return this.readSAM;
    }

    public void setReadDTC(boolean z) {
        this.readDTC = z;
    }

    public boolean isReadDTC() {
        return this.readDTC;
    }

    public void setMrzStrictCheck(boolean z) {
        this.mrzStrictCheck = Boolean.valueOf(z);
    }

    public boolean isMrzStrictCheck() {
        Boolean bool = this.mrzStrictCheck;
        if (bool != null) {
            return bool.booleanValue();
        }
        return false;
    }

    public int getDefaultReadingBufferSize() {
        return this.defaultReadingBufferSize;
    }

    public void setDefaultReadingBufferSize(int i) {
        this.defaultReadingBufferSize = i;
    }

    public Boolean getProceedReadingAlways() {
        return Boolean.valueOf(this.proceedReadingAlways);
    }

    public void setProceedReadingAlways(boolean z) {
        this.proceedReadingAlways = z;
    }
}
