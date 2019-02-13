//
//  VNCBearerSupportingAPI.m
//
//  Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
//

#import "NSData+Base64.h"

#import "VNCBearerSupportingAPI.h"
#import "VNCConnectionWrapper.h"

void * VNCServerBearer_Alloc(size_t size){
	return calloc(1, size);
}

void VNCServerBearer_Free(void * ptr){
	free(ptr);
}

static void * VNCCALL VNCServerBearerAPI_Alloc(VNCBearerContext context, size_t size){
	return VNCServerBearer_Alloc(size);
}

static void VNCCALL VNCServerBearerAPI_Free(VNCBearerContext context, void * ptr){
	VNCServerBearer_Free(ptr);
}

static void VNCCALL VNCServerBearerAPI_Log(VNCBearerContext bearerContext, const char * text){
	NSLog(@"VNC Automotive Server Bearer Log: %s", text);
}

static char* VNCServerBearer_AllocCString(NSString* string) {
	const size_t cStringSize = [string lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
	
	char * cStringData = (char *) VNCServerBearer_Alloc(cStringSize + 1);
	[string getCString:cStringData maxLength:cStringSize+1 encoding:NSUTF8StringEncoding];
	
	return cStringData;
}

static char * VNCCALL VNCServerBearerAPI_GetCommandStringField(VNCConnectionContext context,
		const char * name, int base64decode, size_t * pSize){
	id<VNCBearerConnectionContext> connectionContext = ((VNCConnectionWrapper*) context).context;
	NSString * fieldName = [NSString stringWithCString:name encoding:NSUTF8StringEncoding];
	
	NSLog(@"Bearer connection: field '%@' was requested.", fieldName);
	
	NSString * fieldValue = [connectionContext getCommandStringField:fieldName];
	
	if (fieldValue == nil) {
		NSLog(@"Command string field '%@' doesn't exist.", fieldName);
		return NULL;
	}
	
	if (base64decode != 0) {
		// If base64-decoding is requested.
		NSData* decodedFieldValue = nil;
		@try {
			decodedFieldValue = [NSData dataFromBase64String:fieldValue];
		}
		@catch (NSException *exception) {
			NSLog(@"Failed to Base64-decode value '%@' for command string field '%@'.",
				fieldValue, fieldName);
			return NULL;
		}
		
		if (decodedFieldValue == nil) {
			NSLog(@"Failed to Base64-decode value '%@' for command string field '%@'.",
				fieldValue, fieldName);
			return NULL;
		}
		
		void* decodedFieldValueCData = VNCServerBearer_Alloc((size_t) [decodedFieldValue length]);
		if (pSize != NULL) *pSize = (size_t) [decodedFieldValue length];
		
		[decodedFieldValue getBytes:decodedFieldValueCData range:NSMakeRange(0, [decodedFieldValue length])];
		
		NSLog(@"Got command string field '%@', with Base64 encoded value '%@'.",
			fieldName, fieldValue);
	
		return decodedFieldValueCData;
	} else {
		char* fieldValueCString = VNCServerBearer_AllocCString(fieldValue);
		if (pSize != NULL) *pSize = strlen(fieldValueCString);
		
		NSLog(@"Got command string field '%@', with value '%@' (encoded to C string as '%s').",
			fieldName, fieldValue, fieldValueCString);
	
		return fieldValueCString;
	}
}

static void VNCCALL VNCServerBearerAPI_ConnectionStatusChange(VNCConnectionContext context,
                                                       VNCConnectionStatus status){
    id<VNCBearerConnectionContext> connectionContext = ((VNCConnectionWrapper*) context).context;
    [connectionContext connectionStatusChange:status];
}

static void VNCCALL VNCServerBearerAPI_ConnectionSetTimer(VNCConnectionContext context, int timeoutMs){
	VNCConnectionWrapper* connectionWrapper = (VNCConnectionWrapper*) context;
	if (timeoutMs > 0) {
		[connectionWrapper setTimer:((NSTimeInterval) timeoutMs)/1000.0];
	} else {
		[connectionWrapper cancelTimer];
	}
}
    
static void VNCCALL VNCServerBearerAPI_ConnectionLog(VNCConnectionContext context, const char * text){
	id<VNCBearerConnectionContext> connectionContext = ((VNCConnectionWrapper*) context).context;
	[connectionContext log:[NSString stringWithCString:text encoding:NSUTF8StringEncoding]];
}

static VNCBearerError VNCServerBearerAPI_LocalFeatureCheck(VNCConnectionContext context,
		const unsigned* featureIds, size_t featureIdCount, int* pResult) {
	if (featureIds == NULL || featureIdCount == 0 || pResult == NULL) {
		return VNCBearerErrorInvalidParameter;
	}
	
	id<VNCBearerConnectionContext> connectionContext = ((VNCConnectionWrapper*) context).context;
	
	NSMutableArray* featureArray = [NSMutableArray arrayWithCapacity:featureIdCount];
	for (size_t i = 0; i < featureIdCount; i++) {
		[featureArray addObject:[NSNumber numberWithUnsignedInteger:featureIds[i]]];
	}
	
	const BOOL result = [connectionContext localFeatureCheck:featureArray];
	*pResult = result ? 1 : 0;
	
	return VNCBearerErrorNone;
}

static char* VNCServerBearerAPI_GetBearerConfiguration(VNCConnectionContext context) {
	id<VNCBearerConnectionContext> connectionContext = ((VNCConnectionWrapper*) context).context;
	
	NSString* bearerConfiguration = [connectionContext getBearerConfiguration];
	if (bearerConfiguration == nil) {
		NSLog(@"No bearer configuration.");
		return NULL;
	}
	
	char* bearerConfigurationCString = VNCServerBearer_AllocCString(bearerConfiguration);
	
	NSLog(@"Got bearer configuration '%@' (encoded to C string as '%s').",
		bearerConfiguration, bearerConfigurationCString);
	
	return bearerConfigurationCString;
}

void VNCServerPopulateBearerSupportingAPI(VNCBearerSupportingAPI* supportingAPI) {
	memset((void *) supportingAPI, 0, sizeof(VNCBearerSupportingAPI));
	
	supportingAPI->vncBearerAlloc = VNCServerBearerAPI_Alloc;
	supportingAPI->vncBearerFree = VNCServerBearerAPI_Free;
	supportingAPI->vncBearerLog = VNCServerBearerAPI_Log;
	supportingAPI->vncConnectionGetCommandStringField = VNCServerBearerAPI_GetCommandStringField;
	supportingAPI->vncConnectionStatusChange = VNCServerBearerAPI_ConnectionStatusChange;
	supportingAPI->vncConnectionSetTimer = VNCServerBearerAPI_ConnectionSetTimer;
	supportingAPI->vncConnectionLog = VNCServerBearerAPI_ConnectionLog;
	supportingAPI->vncConnectionLocalFeatureCheck = VNCServerBearerAPI_LocalFeatureCheck;
	supportingAPI->vncConnectionGetBearerConfiguration = VNCServerBearerAPI_GetBearerConfiguration;
}

