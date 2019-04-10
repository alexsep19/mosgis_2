define ([], function () {
    
    var grid_name = 'payment_document_common_comp_cost_grid'
    
    function splash_edit () {
    
        alert ('Чтобы менять содержимое строк начисления, следует перейти в режим правки (кнопка "Редактировать")')

        var b = $('#tb_payment_document_common_comp_cost_grid_toolbar_item_edit')
                
        function colorize (now) {
            b.css ('background-color', 'rgba(255,255,0,' + 0.8 * now + ')')
        }        
        
        b.animate ({aaa: 1}, {step: colorize}, 1000)
        b.animate ({aaa: 0}, {step: colorize}, 1000)
                
    }
    
    function is_yellow (row, col) {

        switch (col.field) {

            case 'cost':
                return true

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

            var t = grid.toolbar            

            if (is_editing) {
                t.enable ('cancel')
                t.disable ('edit')
//                grid.selectNone ()
                w2ui ['payment_document_common_form'].lock ()
                w2ui ['passport_layout'].get ('main').tabs.disable ('payment_document_common_additional_information', 'payment_document_common_log')
                grid.records = data.lines
                grid.refresh ()
            }
            else {
                reload_page ()
            }

            grid.show.selectionBorder = is_editing

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
            
            columns: [                

                {field: 'label', caption: 'Виды начислений', size: 50},
                {field: 'cost', caption: 'Сумма, руб.', size: 100, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2', is_to_sum: 1},

            ],

            records: data.lines.filter (function (r) {return r.cost || r.recid == 'total'}),
            
            onDblClick: null,
            
            onChange: $_DO.patch_payment_document_common_comp_cost,
            
            onEditField: function (e) {
                        
                if (!is_editing) {
                    if (it._can.edit) splash_edit ()
                    return e.preventDefault ()
                }

                var grid = this
                
                var r = grid.get (e.recid)
                
                if (!r.id_type) return e.preventDefault ()
                
                if (e.column == 0 && r.id_type != 50) return e.preventDefault ()

            },            
            
            onRefresh: function (e) {
            
                var grid = this
                                                
                var sum = 0.0

                $.each (grid.records, function () {

                    if (this.w2ui) {
                    
                        var chg = this.w2ui.changes
                        if (!chg) return

                        for (var field in chg) {
                            var col = grid.getColumn (field)
                            var editable = col.editable
                            if (!editable) continue
                            this [field] = chg [field]
                            delete this.w2ui.changes
                        }
                        
                    }
                    
                    var r = this
                    if (r.recid != 'total' && r.cost) {
                        var cost = parseFloat (r.cost)
                        if (cost > 0) sum += cost
                    }

                })

                grid.set ('total', {cost: sum})
            
                e.done (function () {
                    
                    var last = null

                    $.each (data.lines, function () {
                    
                        var row = this

                        var sel = 'tr[recid=' + this.recid + ']'

                        if (!('id_type' in this)) {

                            $(sel + ' td.w2ui-grid-data:not(:last-child)').css ({
                                'font-weight': 'bold',
                                'border-bottom-width': '1px',
                                'border-bottom-color': '#aaa',
                            })
                            
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