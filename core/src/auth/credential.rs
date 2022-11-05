use serde::{Deserialize, Serialize};

pub const CREDENTIAL_TYPE_SSH: i8 = 1;
pub const CREDENTIAL_TYPE_MOSH: i8 = 2;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub enum CredentialType {
    SSH,
    MOSH,
}

pub fn match_credential_type(ctype: i8) -> CredentialType {
    match ctype {
        CREDENTIAL_TYPE_SSH => CredentialType::SSH,
        CREDENTIAL_TYPE_MOSH => CredentialType::MOSH,
        _ => panic!(
            "Credential type only support 1=[SSH], 2=[MOSH]. Get {}",
            ctype
        ),
    }
}

pub fn to_credential_type_const(credential_type: &CredentialType) -> i8 {
    match credential_type {
        CredentialType::SSH => CREDENTIAL_TYPE_SSH,
        CredentialType::MOSH => CREDENTIAL_TYPE_MOSH,
    }
}

#[derive(Serialize, Deserialize)]
pub struct SessionCredentialGroup {
    pub name: &'static str
}

#[derive(Serialize, Deserialize)]
pub struct SessionCredential {
    id: i32,

    pub host: String,
    pub user: String,
    pub password: String,
    pub group: String,
    pub port: i32,
    pub credential_type: CredentialType,
}

impl SessionCredentialGroup {
    pub fn new(name: &'static str) -> Self {
        SessionCredentialGroup {
            name
        }
    }
}

impl SessionCredential {
    pub fn new(
        host: String,
        user: String,
        password: String,
        group: String,
        port: i32,
        credential_type: CredentialType,
    ) -> Self {
        SessionCredential {
            id: 0,
            host,
            user,
            password,
            group,
            port,
            credential_type,
        }
    }
}