var require = {
        baseUrl: "/",
        paths:{
            jquery:"third-party/jquery-1.9.1.min",
            jqueryui:"third-party/jquery-ui-1.10.3/ui/minified/jquery-ui.min",
            underscore:"third-party/underscore-min"
        },
        shim:{
            jquery:{
                exports:"$"
            },
            underscore:{
                exports:"_"
            },
            jqueryui:{
                deps:["jquery"],
                exports:"$.fn.dialog"
            }
        }
};