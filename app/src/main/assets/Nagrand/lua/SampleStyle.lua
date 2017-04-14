local function GET_FONT_PATH()
    local engine = GetEngine()
    local properties = engine.properties
    local os = properties["OS"]
    local os_version = properties["OS_Version"]

    if os == "iOS" or os == "iPhone OS" then
        if os_version >= "7.0" and os_version < "8.0" then
            return "/System/Library/Fonts/Cache/STHeiti-Light.ttc"
        elseif os_version >= "8.0" and os_version < "9.0" then
            return "/System/Library/Fonts/Core/STHeiti-Light.ttc"
        elseif os_version >= "9.0" then
            return "/System/Library/Fonts/LanguageSupport/PingFang.ttc"
        else
            return "/System/Library/Fonts/LanguageSupport/PingFang.ttc"
        end
    elseif os == "Android" then
        return properties["lua_path"] .. "/DroidSansFallback.ttf"
    else
        return ""
    end
end

local function GET_ICON_CACHE_PATH()
    local engine = GetEngine()
    local properties = engine.properties
    local os = properties["OS"]

    if os == "iOS" or os == "iPhone OS"  then
        local icon_path = properties["iconCachePath"]
        return icon_path
    elseif os == "Android" then
        local path = properties["cache_folder"]
        local icon_path = path .. "/icon/"
        return icon_path
    else
        return ""
    end
end

local function SetPolygonStyle_3D(facecolor, styleheight, linecolor, linewidth, face_on_bottom, alpha, align)
    return {
        ['2d'] = {
            style = 'polygon',
            face = {
                enable_alpha = alpha or false,
                color = facecolor or '0xFFF9F5BA',
            },
            outline = {
                color = linecolor or '0xFF999999',
                width = linewidth or 0.05,
                enable_alpha = alpha or false,
                alignment = align or 'AlignRight',
                enable_width = false,
            },
        },

        ['3d'] = {
            style = 'polygon',
            face_on_bottom = face_on_bottom or false, --为false时 height才有效
            height = styleheight or 3,
            face = {
                color = facecolor or '0xFFF9F5BA',
                enable_alpha = alpha or false,
            },
            outline = {
                color = linecolor or '0xFF999999',
                width = linewidth or 0.05,
                height = styleheight or 3,
                enable_alpha = alpha or false,
                enable_edge_shadow = true,
                alignment = align or 'AlignRight',
                enable_width = false,
            },
        },
    }
end

local function SetPolygonStyle_2D(face_color, line_Color)
    return {
        ['2d'] = {
            style = 'polygon',
            face = {
                color = face_color or '0xFFF9F5BA',
                enable_alpha = false,
            },
            outline = {
                color = line_Color or '0xFF999999',
                enable_alpha = false,
                enable_width = false,
                width = 0.05,
            },
        },
    }
end

local function NAVIGATE_PASS_LINE()
    return {
        ['2d'] = {
            style = 'linestring',
            color = '0xAA000000', -- 颜色
            width = 1, -- 线宽
            line_style = 'NONE', -- 线型，NONE、ARROW、DASHED
            has_arrow = true, -- 是否绘制方向指示箭头，仅在line_style为NONE时有效
            has_start = true, -- 绘制起始点
            has_end = true, -- 绘制终点
            enable_alpha = true,
            alignment = 'AlignCenter',
        },
    }
end

local function MULTIPOINT_STYLE()
    return {
        ['2d'] = {
            style = 'multipoint',
            size = 15,
            color = '0xFFCD5E40',
            shape = 'Circle', -- Circle,Square,Heatmap,Sphere,Cylinder,Cubiod,Icon
        },
    }
end

-- 空类型样式，设置此类型的POI不渲染
local function NULLSTYLE()
    return {
        ['2d'] = {
            style = 'nullstyle',
        }
    }
end

local function ANNOTATION(fontcolor, outlinecolor)
    return {
        ['2d'] = {
            style = 'annotation',
            color = fontcolor or '0xFF000000',
            field = 'name',
            unit = 'pt', -- 文字大小(size)使用的单位,"px"表示像素,"pt"表示1/72英寸
            size = 5,
            height = 1,
            outline_color = outlinecolor or '0xAAFFFFFF',
            outline_width = 1,
            anchor_x = 0.5,
            anchor_y = 0.5,
            aabbox_extend = 5,
            enable_fadein = true, -- 开启显隐效果
            -- level = 1,

            -- 添加图标
            anchor_style = {
                style = 'icon',
                -- icon = "icon.png", -- 只要配置了当前属性，就加载本地图片
                icon_url = 'http://api.ipalmap.com/logo/64/',
                icon_cache = GET_ICON_CACHE_PATH(),
                icon_online = 'logo',
                anchor_x = 0.5,
                anchor_y = 0.5,
                use_texture_origin_size = false,
                unit = 'pt', -- 图标大小(width、height)使用的单位,"px"表示像素,"pt"表示点，等于1/72英寸
                width = 7,
                height = 7,
            },
        },
    }
end

-- 以下为地图样式配置
CONFIG = {
    views = {
        default = {
            -- back_color = '0xFFEFE4B0', -- 设置地图背景颜色
            -- back_image = "back.jpg", -- 设置地图背景图片
            layers = {
                -- Frame层
                Frame = {
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'polygon',
                            face = {
                                color = '0xfffffdf8',
                            },
                            outline = {
                                color = '0xffab893e', --外部边框
                                width = 1,
                                alignment = 'AlignLeft', -- 多边形外框线对齐方式设置, 取值为:'AlignLeft'、'AlignCenter'、'AlignRight',沿顺时针方向分别表示居左(外)、居中、居右(内)对齐
                                enable_width = true,
                            },
                            -- multi_polygon = {},
                        },
                    },
                }, -- End Frame

                -- Area层
                Area = {
                    height_offset = 0,
                    renderer = {
                        type = 'unique',
                        key = {
                            'id',
                            'category',
                        },
                        default =  SetPolygonStyle_3D('0xfff4eee1', 5),
                        styles = {
                            [23062000] = SetPolygonStyle_3D('0xffFFFFFF', 5),
                            [23999000] = SetPolygonStyle_3D('0xffe5e0dd', 5),
                            ["230\\d{5}"] = SetPolygonStyle_3D('0xfff0ecc4', 5),
                            ["2409\\d{4}"] = SetPolygonStyle_3D('0xffF0EADC', 5),
                            ["15\\d{6}"] = SetPolygonStyle_3D('0xfff5d5d0', 5),
                            ["13\\d{6}"] = SetPolygonStyle_3D('0xffEAE5F5', 5),
                        },
                        updatestyles = {
                        },
                    },
                }, -- End Area

                -- 文字层
                Area_text = {
                    collision_detection = true, -- 开启文字碰撞检测
                    font_path = GET_FONT_PATH(),
                    renderer = {
                        type = 'unique',
                        key = {
                            'id',
                            'category',
                        },
                        default = ANNOTATION('0xFF606060','0xFFFFFFFF'),
                        styles = {
                        },
                    },
                }, -- End Area_text

                -- 公共设施层
                Facility = {
                    height_offset = -0.2;
                    collision_detection = true,
                    renderer = {
                        type = 'unique',
                        key = {
                            'id',
                            'category',
                        },
                        default = {
                            ['2d'] = {
                                style = 'icon',
                                -- icon = "icon.png", -- 只要配置了当前属性，就加载本地图片
                                icon_url = 'http://api.ipalmap.com/logo/64/',
                                icon_cache = GET_ICON_CACHE_PATH(),
                                icon_online = 'logo',
                                anchor_x = 0.5,
                                anchor_y = 0.5,
                                use_texture_origin_size = false,
                                unit = 'pt', -- 图标大小(width、height)使用的单位,"px"表示像素,"pt"表示1/72英寸
                                width = 7,
                                height = 7,
                                enable_fadein = true, -- 开启显隐效果
                                -- level = 1,
                            },
                        },
                        tyles = {
                        },
                        updatesstyles = {
                        },
                    },
                }, -- End Facility

                -- 定位点
                positioning = {
                    height_offset = -0.4,
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'icon',
                            icon = 'locate.png', -- 根据实际情况配置图标路径
                            use_texture_origin_size = true,
                            width = 25,
                            height = 25,
                            anchor_x = 0.0,
                            anchor_y = 0.0,
                        },
                    },
                }, -- End positiong

                -- 导航层
                navigate = {
                    height_offset = -1,
                    renderer = {
                        type = 'unique',
                        key = {
                            'navi_name', -- 经停点默认使用这个字段区别导航线和经停点
                        },
                        default = {
                            ['2d'] = {
                                style = 'linestring',
                                color = '0xFF64b5f6', -- 颜色
                                width = 1, -- 线宽
                                line_style = 'NONE', -- 线型，NONE、ARROW、DASHED
                                has_arrow = true, -- 是否绘制方向指示箭头，仅在line_style为NONE时有效
                                has_start = true, -- 绘制起始点
                                has_end = true, -- 绘制终点
                                enable_alpha = true,
                                alignment = 'AlignCenter',
                            },
                        },
                        styles = {
                            ["transit"] = MULTIPOINT_STYLE(), -- 这个是固定匹配经停点的属性
                            ["dyna_pass"] = NAVIGATE_PASS_LINE(),
                            ["dyna_remain"] = default,
                        },
                    },
                }, -- End naivgate
                -- 添加多边形
                polygon = {
                    height_offset = 3.2,
                    renderer = {
                        key = {
                            'id'
                        },
                        default = SetPolygonStyle_3D('0xfff4eee1', 5),
                        type = 'unique',
                    }
                },
                -- 添加折线
                linestring = {
                    height_offset = 3.2,
                    renderer = {
                        key = {
                            'id'
                        },
                        type = 'unique',
                        default = {
                            ['2d'] = {
                                style = 'linestring',
                                color = '0xFF64b5f6', -- 颜色
                                width = 1, -- 线宽
                                line_style = 'NONE', -- 线型，NONE、ARROW、DASHED
                                has_arrow = true, -- 是否绘制方向指示箭头，仅在line_style为NONE时有效
                                has_start = true, -- 绘制起始点
                                has_end = true, -- 绘制终点
                                enable_alpha = true,
                                alignment = 'AlignCenter',
                            },
                        },
                    }
                },

            }, -- End layers
        }, -- End default
    }, -- End views
} -- End CONFIG
