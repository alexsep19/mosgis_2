define ([], function () {
        
    var fld = {
        
        Enum: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()

            return {
                field: fn,
                caption: o.remark,
                size: 50,
                sortable: true,
                render: function (i) {
                    var v = i [fn]
                    return v.split ('|').sort ().join (', ')
                }
            }
            
        },

        Date: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()

            return {
                field: fn,
                caption: o.remark,
                size: 16,
                sortable: true,
                render: function (i) {
                    var v = i [fn]
                    return dt_dmy (v.substr (0, 10))
                }
            }
            
        },
        
        String: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()

            return {
                field: fn,
                caption: o.remark,
                size: 50,
                sortable: true
            }
            
        },
        
        Boolean: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()
            
            return {
                field: fn,
                caption: o.remark,
                size: 5,
                sortable: true,
                render: function (i) {
                    var v = i [fn]
                    return v == null ? '' : v == 1 ? 'Да' : 'Нет'
                }
            }

        },
        
        OkeiRef: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()

            return {
                field: fn,
                caption: o.remark,
                sortable: true,
                size: 10,
                render: function (i) {
                    var v = i [fn]
                    return v == null ? '' : $('body').data ('data').vc_okei [v]
                }
            }
            
        },
        
        NsiRef: function (o) {
            
            var fn = "f_" + o.name.toLowerCase ()
            
            return {
                field: fn,
                caption: o.remark,
                sortable: true,
                size: 100,
                render: function (i) {
                    var v = i [fn]
                    if (!v) return ''
                    var t = $('body').data ('data') ['vc_nsi_' + o.ref]
                    if (!t) return '[не загружен справочник]'
                    if (!o.mul) return t [v]
                    return v.map (function (i) {return t [i]}).join (', ')
                }
            }
            
        },

        NsiListRef: function (o) {
            
            var fn = "F_" + o.name

            return {
                field: fn,
                caption: o.remark,
                sortable: true,
                size: 100,
                render: function (i) {
                    var v = i [fn]
                    if (!v) return ''
                    return $('body').data ('vc_nsi_list') [v] || v
                }
            }
            
        },
        
//            $('body').data ('vc_nsi_list', vc_nsi_list)
        
        
    }
        
    return function (data, view) {
        
        var vocs_layout = w2ui ['vocs_layout']
        
        if (!vocs_layout) $('title').text (data.item.name)
        
        var $main = vocs_layout ? $(w2ui ['vocs_layout'].el ('main')) : $('body')
                
        var layout = $main.empty ().w2relayout ({

            name: 'voc_layout',

            panels: [
                {type: 'top', size: 30, content: "foo"},
                {type: 'main', size: 400},                
            ],            
            
        });
        
        fill (view, data, $(layout.el ('top')))
        
        var columns = [
            {field: 'code',    caption: 'Код',    size: 5, sortable: true},
            {field: 'id',      caption: 'ID',     size: 30, sortable: true},
        ]

        if (data.item.cols && data.item.cols.length) $.each (data.item.cols, function () {
            
            var f = fld [this.type] || fld ['String']
            
            columns.push (f (this))
        
        })
        
        $(layout.el ('main')).w2regrid ({
            
            name: 'nsi_grid_' + data.item.registrynumber,
            reorderColumns: true,
            
            toolbar: {
                items: [
                    {type: 'button', caption: 'Импорт справочника из ГИС ЖКХ...', onClick: $_DO.import_voc_nsi},
                    {type: 'button', id: 'printButton', caption: 'MS Excel', onClick: function () {this.owner.saveAsXLS ()}},
                ],
            },

            show: {
                toolbar: true,
                footer: true,
            },

            url: '/mosgis/_rest/?type=voc_nsi_list&part=lines&id=' + data.id,

            columns: columns,

            onRender: $_DO.check_status_voc_nsi,
            
            onDblClick: null,            
            
        }).refresh ()

    }

});