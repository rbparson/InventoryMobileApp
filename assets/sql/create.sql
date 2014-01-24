CREATE TABLE AM12ACTIVITY (
   nuxractivity INTEGER PRIMARY KEY AUTOINCREMENT,
   naactivity TEXT NOT NULL,
   nuxracttype INTEGER NOT NULL, 
   dttxnorigin TEXT NOT NULL,
   natxnorguser TEXT NOT NULL, 
   dttxnupdate TEXT NOT NULL,
   natxnupduser TEXT NOT NULL 
);
CREATE TABLE AL112ACTTYPE (
   nuxracttype  INTEGER PRIMARY KEY AUTOINCREMENT,
   deacttype TEXT NOT NULL
);

INSERT INTO AL112ACTTYPE (deacttype) VALUES ('VERIFICATION');
INSERT INTO AL112ACTTYPE (deacttype) VALUES ('PICKUP');
INSERT INTO AL112ACTTYPE (deacttype) VALUES ('DELIVERY');
INSERT INTO AL112ACTTYPE (deacttype) VALUES ('EDITPICKUP');
INSERT INTO AL112ACTTYPE (deacttype) VALUES ('SEARCH');

CREATE TABLE AM12VERIFY (
   nuxrverifyloc  INTEGER PRIMARY KEY AUTOINCREMENT,
   Nuxractivity Integer  Not Null,
   cdlocat TEXT NOT NULL,
   Cdloctype Text Not Null,
   locationentry TEXT,
   locationtoentry TEXT,
   cdrespctrhd TEXT,
   adstreet1 TEXT,
   adcity TEXT,
   adstate TEXT,
   adzipcode,
   descript TEXT,
   dttxnorigin TEXT NOT NULL,
   natxnorguser TEXT NOT NULL, 
   dttxnupdate TEXT NOT NULL,
   natxnupduser TEXT NOT NULL   
);

CREATE TABLE ad12verinv(
   nuxrverifyloc INTEGER PRIMARY KEY AUTOINCREMENT,
   nuxractivity INTEGER NOT NULL,   
   nusenate TEXT NOT NULL, 
   cdcond TEXT NOT NULL,
   cdcategory TEXT,
   cdintransit TEXT NOT NULL, 
   decommodityf TEXT NOT NULL, 
   cdlocat TEXT NOT NULL, 
   cdloctype TEXT NOT NULL,  
   cdcommodity TEXT,
   decomments TEXT,
   dttxnorigin TEXT NOT NULL,
   natxnorguser TEXT NOT NULL, 
   dttxnupdate TEXT NOT NULL,
   natxnupduser TEXT NOT NULL
);

Create Table Am12pickup (
   nuxrpickuploc  INTEGER PRIMARY KEY AUTOINCREMENT,
   Nuxractivity Integer  Not Null,
   Cdlocatfrom Text Not Null,
   Cdloctypefrom Text Not Null,
   Locationfromentry Text,
   Cdrespctrhdfrom Text,
   Adstreet1from Text,
   Adcityfrom Text,
   Adstatefrom Text,
   Adzipcodefrom,
   Descriptfrom Text,
   Cdlocatto Text Not Null,
   Cdloctypeto Text Not Null,
   Locationtoentry Text,
   Cdrespctrhdto Text,
   Adstreet1to Text,
   Adcityto Text,
   Adstateto Text,
   Adzipcodeto,
   Descriptto Text,
   Blsign Blob,
   Cdremote Integer,
   Cdremoteshiptype Integer,
   cdpaperworkrequest Text,
   dttxnorigin TEXT NOT NULL,
   natxnorguser TEXT NOT NULL, 
   dttxnupdate TEXT NOT NULL,
   natxnupduser TEXT NOT NULL   
);

CREATE TABLE ad12pickupinv(
   nuxrpickuploc INTEGER PRIMARY KEY AUTOINCREMENT,
   nuxractivity INTEGER NOT NULL,   
   nusenate TEXT NOT NULL, 
   cdcond TEXT NOT NULL,
   cdcategory TEXT,
   cdintransit TEXT NOT NULL, 
   Decommodityf Text Not Null, 
   Cdlocatfrom Text Not Null, 
   Cdloctypefrom Text Not Null,  
   Cdlocatto Text Not Null, 
   cdloctypeto TEXT NOT NULL,  
   cdcommodity TEXT,
   decomments TEXT,
   dttxnorigin TEXT NOT NULL,
   natxnorguser TEXT NOT NULL, 
   dttxnupdate TEXT NOT NULL,
   Natxnupduser Text Not Null
);

