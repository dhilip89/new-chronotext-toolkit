/*
 * THE NEW CHRONOTEXT TOOLKIT: https://github.com/arielm/new-chronotext-toolkit
 * COPYRIGHT (C) 2014, ARIEL MALKA ALL RIGHTS RESERVED.
 *
 * THE FOLLOWING SOURCE-CODE IS DISTRIBUTED UNDER THE MODIFIED BSD LICENSE:
 * https://github.com/arielm/new-chronotext-toolkit/blob/master/LICENSE.md
 */

#include "chronotext/font/zf/FontSequence.h"

using namespace std;

namespace chronotext
{
    namespace zf
    {
        void FontSequence::begin(bool useColor)
        {
            this->useColor = useColor;
            maps.clear();
        }
        
        void FontSequence::end()
        {}
        
        void FontSequence::addMap(unique_ptr<GlyphBatchMap> &&map)
        {
            maps.emplace_back(forward<unique_ptr<GlyphBatchMap>>(map));
        }
        
        void FontSequence::replay(const GLushort *indices)
        {
            for (auto &map : maps)
            {
                map->flush(indices, useColor);
            }
        }
    }
}
