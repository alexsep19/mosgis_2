define ([], function () {
    
    var grid_name = 'payment_document_common_service_info_grid'
    
    function splash_edit () {
    
        alert ('Чтобы менять содержимое строк начисления, следует перейти в режим правки (кнопка "Редактировать")')

        var b = $('#tb_payment_document_common_service_info_grid_toolbar_item_edit')
                
        function colorize (now) {
            b.css ('background-color', 'rgba(255,255,0,' + 0.8 * now + ')')
        }        
        
        b.animate ({aaa: 1}, {step: colorize}, 1000)
        b.animate ({aaa: 0}, {step: colorize}, 1000)
                
    }
    
    function is_yellow (row, col) {

        switch (col.field) {
        
            case 'si_ht_over':
            case 'si_ho_norm':
                return true

            case 'si_val_ind':
            case 'si_val_over':
            case 'si_ht_ind':
            case 'si_ind_norm':
                return row.uuid_m_m_service

            default:
                return false

        }

    }
                
    return function (data, view) {

        var it = data.item
        
        var is_editing = false
        
        function setEditig (v) {

            is_editing = !!v
            
            var grid = w2ui [grid_name]

            grid.show.selectionBorder = is_editing

            var t = grid.toolbar            

            if (is_editing) {
                t.enable ('cancel')
                t.disable ('edit')
                grid.selectNone ()
                w2ui ['payment_document_common_form'].lock ()
                
                var tabs = w2ui ['passport_layout'].get ('main').tabs
                $.each (tabs.tabs, function () {
                    var id = this.id
                    if (id == 'payment_document_common_service_info') return
                    tabs.disable (id)
                })
                
            }
            else {
                reload_page ()
            }

            grid.refresh ()

        }

        var layout = w2ui ['passport_layout']

        var $panel = $(layout.el ('main'))               

        $panel.w2regrid ({ 

            name: grid_name,
            
            selectType: 'cell',
            multiSelect: false,

            show: {
                toolbar: it._can.edit,
                toolbarInput: 0,
                toolbarReload: 0,
                toolbarColumns: 0,
                footer: 1,
                selectionBorder: false,
            },            

            toolbar: {            
                items: [
                    {type: 'button', id: 'edit', caption: 'Редактировать', onClick: function () {setEditig (1)}, disabled: false, icon: 'w2ui-icon-pencil'},
                    {type: 'button', id: 'cancel', caption: 'Зафиксировать', onClick: function () {setEditig (0)}, disabled: true, icon: 'w2ui-icon-check'},
                ],
            },                        

            columnGroups : [            
                {master: true},
                {span: 2, caption: 'Текущие показания ПУ'},
                {span: 2, caption: 'Суммарный объём коммунальных услуг'},
                {span: 2, caption: 'Норматив потребления коммунальных услуг'},
                {span: 2, caption: 'Объем услуги'},
            ], 

            columns: [                

                {field: 'label', caption: 'Наименование услуги', size: 50},
                
                {field: 'si_val_ind', caption: 'инд.', tooltip: 'индивидуальное потребление', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},
                {field: 'si_val_over', caption: 'ОДН', tooltip: 'общедомовые нужды', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},

                {field: 'si_ht_ind', caption: 'инд.', tooltip: 'индивидуальное потребление', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},
                {field: 'si_ht_over', caption: 'ОДН', tooltip: 'общедомовые нужды', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},

                {field: 'si_ind_norm', caption: 'инд.', tooltip: 'индивидуальное потребление', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},
                {field: 'si_ho_norm', caption: 'ОДН', tooltip: 'общедомовые нужды', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}, render: 'float:7'},

                {field: 'cons_i_vol', caption: 'инд.', tooltip: 'индивидуальное потребление', size: 10, render: 'float:7'},
                {field: 'cons_o_vol', caption: 'ОДН', tooltip: 'общедомовые нужды', size: 10, render: 'float:7'},

            ],

            records: data.lines,
            
            onDblClick: null,
            
            onChange: $_DO.patch_payment_document_common_service_info,
            
            onEditField: function (e) {
                        
                if (!is_editing) {
                    if (it._can.edit) splash_edit ()
                    return e.preventDefault ()
                }

                var grid = this
                
                var r = grid.get (e.recid)
                
                if (!is_yellow (r, grid.columns [e.column])) return e.preventDefault ()

                if (!r.id_type) return e.preventDefault ()
                
                if (e.column == 0 && r.id_type != 50) return e.preventDefault ()

            },            
            
            onRefresh: function (e) {
            
                var grid = this
                
                $.each (grid.records, function () {

                    if (!this.w2ui) return
                    var chg = this.w2ui.changes
                    if (!chg) return

                    for (var field in chg) {
                        var col = grid.getColumn (field)
                        var editable = col.editable
                        if (!editable || editable.type != 'list') continue
                        this [field] = chg [field].uuid
                        delete this.w2ui.changes
                    }

                })
            
                e.done (function () {
                    
                    var last = null

                    $.each (data.lines, function () {
                    
                        var row = this

                        var sel = 'tr[recid=' + this.recid + ']'

                        if (!('id_type' in this)) {


                            $(sel + ' td.w2ui-grid-data:not(:last-child)').css ({
                                'font-weight': 'bold',
                                'border-bottom-width': '1px',
                            }).css ({'border-right-width': 0})
                            
                            if (last) {
                            
                                sel = 'tr[recid=' + last + ']'

                                $(sel + ' td.w2ui-grid-data').css ({
                                    'border-bottom-width': '1px',
                                })
                            
                            }
                        
                        }
                        else if (is_editing) {
                        
                            $(sel + ' td.w2ui-grid-data').each (function () {

                                var $this = $(this)
                                var col = grid.columns [$this.attr ('col')]
                                
                                if (is_yellow (row, col)) $this.css ({background: '#ffffcc'})

                            })
                        
                        }
                        
                        last = this.id                        

                    })

                }) 
            
            }

        }).refresh ()

    }
    
})