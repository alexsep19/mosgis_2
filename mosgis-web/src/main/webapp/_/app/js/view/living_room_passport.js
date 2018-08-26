define ([], function () {
    
    var grid_name = 'living_room_passport_grid'
    var id2field = {}
    
    function getData () {
        return $('body').data ('data')
    }   
        
    return function (data, view) {

        $.each (data.vc_pass_fields, function () { id2field [this.id] = this })

        $_F5 = function () {

            var grid = w2ui [grid_name]

            var data = getData ()

            var doc_fields = data.doc_fields

            grid.records = data.vc_pass_fields.filter (function (i) {
            
                if (i.id_type == 3) return false

                return true

            })
            
            $.each (grid.records, function () {
                if (this.w2ui) delete this.w2ui.changes
                this.is_doc = doc_fields [this.id]
                delete this.value
            })

            grid.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
            },            

            textSearch: 'contains',

            columns: [                
//                {field: 'ord_src', caption: '№ п/п', size: 5},
                {field: 'label', caption: 'Реквизит', size: 100},
                {field: 'value', caption: 'Значение', size: 20, attr: 'data-status', editable: function (voc) {                
                    switch (voc.id_type) {
                        case 0:  return {type: 'int'}
                        case 1:  return {type: 'float', precision: 4, autoFormat: true, keyboard: false, min: 0}
                        case 2:  return {type: 'text'}
                        case 4:  return {type: 'list', items: [{id: 0, text: "Нет"}, {id: 1, text: "Да"}]}
                        case 6:
                            var voc = getData () ['vc_nsi_' + voc.voc]
                            return !voc ? null : {type: 'list', items: voc.items}
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
                                var ids = d ['tb_living_rooms_f_' + voc.id]
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
            ],

            records: [],
            
            onDblClick: null,
            
            onEditField: function (e) {

                if (getData ().item.is_annuled) {
                    alert ('Поскольку данный объект жилого фонда аннулирован, редактирование невозможно.')
                    return e.preventDefault ()
                }
                
                var grid     = this
                var record   = grid.get (e.recid)
                
                if (record.is_doc) {
                    alert ('Добавление подтверждающих документов осуществляется на вкладке "Документы"')
                    return e.preventDefault ()
                }
                
                if (record.is_multiple) {
                
                    var d = getData ()
                    
                    var o = {
                        def: record,
                        voc: d ['vc_nsi_' + record.voc],
                        ids: d ['tb_living_rooms_f_' + record.id]
                    }
                                                            
                    $_SESSION.set ('field', o)
                    
                    use.block ('passport_multiple_popup')
                    
                    return e.preventDefault ()
                    
                }

                var col      = grid.columns [e.column]
                var editable = col.editable (record)
                var v        = getData ().item ['f_' + e.recid]

                if (record.id_type == 4 && v == 1) {
                    var d = data.depends
                    for (var i in d) if (d [i] == record.id) {
                        var child = id2field [i]
                        if (child.id_dt && getData ().item ['f_' + child.id_dt]) {
                            alert ('Первоначально необходимо удалить ' + child.label.toLowerCase () + ' на вкладке "Документы"')
                            return e.preventDefault ()                            
                        }
                    }
                    
                }                

                if (editable.type == 'date') {
                    e.value = v ? new Date (v) : new Date ()
                }
                else {
                    e.value = v
                }
                
            },
            
            onChange: $_DO.patch_living_room_passport,
            
            onRefresh: color_passport_grid

        })

        $_F5 ()

    }
    
})