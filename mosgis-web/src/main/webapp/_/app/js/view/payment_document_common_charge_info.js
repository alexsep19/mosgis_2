define ([], function () {
    
    var grid_name = 'payment_document_common_charge_info_grid'
                
    return function (data, view) {

        $_F5 = function () {

            var grid = w2ui [grid_name]

            grid.records = data.lines
            
            $.each (grid.records, function () {
                if (this.w2ui) delete this.w2ui.changes
                delete this.value
            })

            grid.refresh ()

        }

        var layout = w2ui ['passport_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 

            name: grid_name,

            show: {
                toolbar: 0,
                footer: 1,
            },            

            textSearch: 'contains',

            columns: [                
//                {field: 'ord_src', caption: '№ п/п', size: 5},
                {field: 'label', caption: 'Наименование услуги', size: 100},
/*                
                {field: 'value', caption: 'Значение', size: 20, attr: 'data-status', editable: !data.is_passport_editable ? null : function (voc) {                
                    switch (voc.id_type) {
                        case 0:  return {type: 'int'}
                        case 1:  return {type: 'float', precision: 4, autoFormat: true, keyboard: false, min: 0}
                        case 2:  return {type: 'text'}
                        case 4:  return {type: 'list', items: [{id: 0, text: "Нет"}, {id: 1, text: "Да"}]}
                        case 6:
                            var voc = getData () ['vc_nsi_' + voc.voc]
                            return !voc ? null : {type: 'list', items: voc.items}
                            break;
                        case 7:  return {type: 'int', min: 1600, max: 2215, autoFormat: false}
                        case 8:  return {type: 'date'}
                        default: return null
                    }                
                }, render: function (voc) {
                
                    var v = getData ().item [voc.name]
                    
                    if (voc.is_multiple) {
                    
                        switch (voc.id_type) {                    
                            case 6:
                                var d = getData ()
                                var ids = d ['tb_houses_f_' + voc.id]
                                var s = ''
                                $.each (d ['vc_nsi_' + voc.voc].items, function () {
                                    if (!ids [this.id]) return
                                    if (s) s += ', '
                                    s += this.label
                                })
                                return s
                            default: return ''
                        }                
                    
                    }
                    else {
                    
                        switch (voc.id_type) {                    
                            case 4:  return v == 1 ? 'Да' : 'Нет'
                            case 1:  return v == null ? '' : w2utils.formatNumber (v)
                            case 6:
                                var voc = getData () ['vc_nsi_' + voc.voc]
                                return !voc ? '' : voc [v]
                                break;
                            case 8:  return !v ? '' : dt_dmy (v.substr (0, 10))
                            default: return v
                        }                
                        
                    }
                
                }},
                {field: 'unit', caption: 'Ед. изм.', size: 5},
*/                
            ],

            records: [],
            
            onDblClick: null,
            
            onEditField: function (e) {

                var grid     = this
                var record   = grid.get (e.recid)
                
                var col      = grid.columns [e.column]
                var editable = col.editable (record)
                var v        = getData ().item ['f_' + e.recid]

                if (editable.type == 'date') {
                    e.value = v ? new Date (v) : new Date ()
                }
                else {
                    e.value = v
                }
                
            },
            
            onChange: $_DO.patch_payment_document_common_charge_info,
            
            onRefresh: function (e) {
            
                e.done (function () {
                    
                    var last = null

                    $.each (data.lines, function () {

                        if (!('id_type' in this)) {
                        
                            var sel = 'tr[recid=' + this.id + ']'

                            $(sel + ' td.w2ui-grid-data').css ({
                                'font-weight': 'bold',
                                'border-bottom-width': '1px',
                            }).eq (0).css ({'border-right-width': 0})
                            
                            if (last) {
                            
                                sel = 'tr[recid=' + last + ']'

                                $(sel + ' td.w2ui-grid-data').css ({
                                    'border-bottom-width': '1px',
                                })
                            
                            }
                        
                        }
                        
                        last = this.id                        

                    })

                }) 
            
            }

        })

        $_F5 ()

    }
    
})