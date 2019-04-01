define ([], function () {
    
    var grid_name = 'payment_document_common_piecemeal_payment_grid'
    
    function splash_edit () {
    
        alert ('Чтобы менять содержимое строк начисления, следует перейти в режим правки (кнопка "Редактировать")')

        var b = $('#tb_payment_document_common_piecemeal_payment_grid_toolbar_item_edit')
                
        function colorize (now) {
            b.css ('background-color', 'rgba(255,255,0,' + 0.8 * now + ')')
        }        
        
        b.animate ({aaa: 1}, {step: colorize}, 1000)
        b.animate ({aaa: 0}, {step: colorize}, 1000)
                
    }
    
    function is_yellow (row, col) {

        switch (col.field) {

            case 'pp_pp_sum':
            case 'pp_ppp_sum':
            case 'pp_rate_rub':
            case 'pp_rate_prc':
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

            grid.show.selectionBorder = is_editing

            var t = grid.toolbar            

            if (is_editing) {
                t.enable ('cancel')
                t.disable ('edit')
                grid.selectNone ()
                w2ui ['payment_document_common_form'].lock ()
                w2ui ['passport_layout'].get ('main').tabs.disable ('payment_document_common_additional_information', 'payment_document_common_log')
            }
            else {
                t.disable ('cancel')
                t.enable ('edit')
                w2ui ['payment_document_common_form'].unlock ()
                w2ui ['passport_layout'].get ('main').tabs.enable ('payment_document_common_additional_information', 'payment_document_common_log')
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
                {master: true},                
                {span: 2, caption: 'С учётом рассрочки'},
                {span: 2, caption: 'Проценты за рассрочку'},
            ], 
            
            columns: [                

                {field: 'label', caption: 'Наименование услуги', size: 50},
                {field: 'pp_sum', caption: 'К оплате', tooltip: 'Сумма к оплате с учётом рассрочки платежа и процентов за рассрочку', size: 10, render: 'float:2', is_to_sum: 1},
                {field: 'pp_pp_sum', caption: 'за этот период', tooltip: 'Сумма платы с учётом рассрочки платежа - от платы за расчётный период, руб', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2', is_to_sum: 1},
                {field: 'pp_ppp_sum', caption: 'за прошлые периоды', tooltip: 'Сумма платы с учётом рассрочки платежа - от платы за предыдущие расчётные периоды, руб', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2', is_to_sum: 1},
                {field: 'pp_rate_rub', caption: 'руб.', tooltip: 'Проценты за рассрочку, руб', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2', is_to_sum: 1},
                {field: 'pp_rate_prc', caption: '%', tooltip: 'Проценты за рассрочку, %', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2'},

            ],

            records: data.lines,
            
            onDblClick: null,
            
            onChange: $_DO.patch_payment_document_common_piecemeal_payment,
            
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
                
                var sum = {}
                
                if (this.recid != 'total') $.each (grid.columns, function () {
                    if (!this.is_to_sum) return
                    var k = this.field
                    sum [k] = sum [k] || 0
                })
                
                $.each (grid.records, function () {

                    if (this.w2ui) {
                    
                        var chg = this.w2ui.changes
                        if (!chg) return

                        for (var field in chg) {
                            var col = grid.getColumn (field)
                            var editable = col.editable
                            if (!editable || editable.type != 'list') continue
                            this [field] = chg [field].uuid
                            delete this.w2ui.changes
                        }
                        
                    }
                    
                    var r = this
                    
                    if (this.recid != 'total') $.each (grid.columns, function () {
                        if (!this.is_to_sum) return
                        var k = this.field
                        var v = r [k]
                        if (v != null) sum [k] += parseFloat (v)
                    })

                })

                grid.set ('total', sum)
            
                e.done (function () {
                    
                    var last = null

                    $.each (data.lines, function () {
                    
                        var row = this

                        var sel = 'tr[recid=' + this.recid + ']'

                        if (!('id_type' in this)) {

                            $(sel + ' td.w2ui-grid-data:not(:last-child)').css ({
                                'font-weight': 'bold',
                                'border-bottom-width': '1px',
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