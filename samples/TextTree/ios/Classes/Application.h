/*
 * THE NEW CHRONOTEXT TOOLKIT: https://github.com/arielm/new-chronotext-toolkit
 * COPYRIGHT (C) 2012-2014, ARIEL MALKA ALL RIGHTS RESERVED.
 *
 * THE FOLLOWING SOURCE-CODE IS DISTRIBUTED UNDER THE SIMPLIFIED BSD LICENSE:
 * https://github.com/arielm/new-chronotext-toolkit/blob/master/LICENSE.md
 */

/*
 * STUB FOR RUNNING A CinderSketch ON iOS
 */

#pragma once

#import "CinderBridge.h"

@interface Application : NSObject <UIApplicationDelegate>
{
    CinderBridge *bridge;
    UIWindow *window;
}

@end
