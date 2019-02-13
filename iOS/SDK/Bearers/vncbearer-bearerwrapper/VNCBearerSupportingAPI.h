//
//  VNCBearerSupportingAPI.h
//
//  Copyright (C) 2011-2018 VNC Automotive Ltd.  All Rights Reserved.
//

void * VNCServerBearer_Alloc(size_t size);

void VNCServerBearer_Free(void * ptr);

void VNCServerPopulateBearerSupportingAPI(VNCBearerSupportingAPI* bearerSupportingAPI);
