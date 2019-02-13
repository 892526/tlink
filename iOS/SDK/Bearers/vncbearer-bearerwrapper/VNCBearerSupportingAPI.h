//
//  VNCBearerSupportingAPI.h
//
//  Copyright RealVNC Ltd. 2011-2018. All rights reserved.
//

void * VNCServerBearer_Alloc(size_t size);

void VNCServerBearer_Free(void * ptr);

void VNCServerPopulateBearerSupportingAPI(VNCBearerSupportingAPI* bearerSupportingAPI);
