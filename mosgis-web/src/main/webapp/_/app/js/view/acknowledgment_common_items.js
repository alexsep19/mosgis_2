define ([], function () {
    
    var grid_name = 'acknowledgment_common_items_grid'
    
    function splash_edit () {
    
        alert ('Чтобы менять содержимое строк начисления, следует перейти в режим правки (кнопка "Редактировать")')

        var b = $('#tb_acknowledgment_common_items_grid_toolbar_item_edit')
                
        function colorize (now) {
            b.css ('background-color', 'rgba(255,255,0,' + 0.8 * now + ')')
        }        
        
        b.animate ({aaa: 1}, {step: colorize}, 1000)
        b.animate ({aaa: 0}, {step: colorize}, 1000)
                
    }
    
    function get_message (row, col) {
    
        switch (col.field) {
/*
            case 'totalpayable':
                var f = row.totalpayable
                var p = row.totalpayable_v
                return f && p && Math.abs (parseFloat (f) - parseFloat (p)) ? 'Значение не сопадает с автоматически рассчитанным: ' + w2utils.formatNumber (p) : null

            case 'accountingperiodtotal':
                var f = row.accountingperiodtotal
                var p = row.accountingperiodtotal_v
                return f && p && Math.abs (parseFloat (f) - parseFloat (p)) ? 'Значение не сопадает с автоматически рассчитанным: ' + w2utils.formatNumber (p) : null
                
            case 'calcexplanation':
                return row.uuid_gen_need_res && !row.calcexplanation ? 'Для данной строки это поле обязательно' : null

            case 'moneyrecalculation':
                return !row.moneyrecalculation && row.recalculationreason ? 'Не заполнена сумма' : null

            case 'recalculationreason':
                return !row.recalculationreason && row.moneyrecalculation ? 'Не заполнено основание' : null

            case 'cons_i_dtrm_meth':
                return row.uuid_m_m_service && row.cons_i_vol != null && !row.cons_i_dtrm_meth ? 'Не указан способ определения' : null

            case 'cons_o_dtrm_meth':
                return (row.uuid_m_m_service || row.uuid_add_service || row.uuid_gen_need_res) && row.cons_o_vol != null && !row.cons_o_dtrm_meth ? 'Не указан способ определения' : null
                
            case 'rate':
                return (row.accountingperiodtotal || row.cons_i_vol || row.cons_o_vol) && row.rate == null ? 'Не указан тариф' : null
*/                                
            default:
                return null

        }        
    
    }
    
    function is_yellow (row, col) {
        switch (col.field) {
            case 'amount':
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
            
                t.enable  ('cancel')
                t.disable ('edit')
                t.disable ('distribute')
                
                grid.selectNone ()
                
                w2ui ['acknowledgment_common_form'].lock ()
                
                var tabs = w2ui ['passport_layout'].get ('main').tabs
                $.each (tabs.tabs, function () {
                    var id = this.id
                    if (id == 'acknowledgment_common_items') return
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
                    {type: 'button', id: 'distribute', caption: 'Распределить', onClick: $_DO.distribute_acknowledgment_common_items, disabled: false},
                ],

            },                        
            
            columnGroups : [
            
                {master: true},
                {master: true},
                {master: true},
                
                {master: true},
                
                {span: 2, caption: 'Индивидульное потребление'},
                {span: 2, caption: 'ОДН'},

                {master: true},
                
                {span: 2, caption: 'Повышающий коэффициент'},
                {span: 2, caption: 'Перерасчет'},
                
                {master: true},
                {master: true},
                {master: true},

            ], 

            columns: [                

                {field: 'label', caption: 'Наименование услуги', size: 50},
                
                {field: 'totalpayable', caption: 'Сумма к оплате с учетом рассрочки платежа и процентов за рассрочку, руб.', size: 10, is_to_sum: 1, render: 'float:2'},
                {field: 'amount_nack', caption: 'Остаток к оплате, руб.', size: 10, is_to_sum: 1, render: 'float:2'},
                {field: 'amount', caption: 'Сумма платежа, руб.', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, render: 'float:2', is_to_sum: 1},

            ],

            records: data.lines,

            onDblClick: null,

            onChange: $_DO.patch_acknowledgment_common_items,

            onEditField: function (e) {

                if (!is_editing) {
                    if (it._can.edit) splash_edit ()
                    return e.preventDefault ()
                }

                var grid = this

                var r = grid.get (e.recid)

                if (!r.id_type || r.id_type < 0) return e.preventDefault ()

                var col = grid.columns [e.column]

                switch (col.field) {

                    case 'amountofexcessfees':
                        if (r.ratio == null) return e.preventDefault ()
                        break

                    case 'ratio':
                        if (!(r.uuid_m_m_service && (r.cons_i_dtrm_meth == 'N' || r.cons_o_dtrm_meth == 'N'))) return e.preventDefault ()
                        break

                    case 'cons_i_vol':
                        if (!r.uuid_m_m_service) return e.preventDefault ()
                        break
                        
                    case 'cons_o_vol':
                        if (!(r.uuid_m_m_service
                            && ($_USER.role.nsi_20_2
//                                || ($_USER.role.nsi_20_36) // ?
                            )
                        )
                            && !r.uuid_add_service 
                            && !r.uuid_gen_need_res
                        ) return e.preventDefault ()
                        break

                }

            },            

            onRefresh: function (e) {
            
                var grid = this
                
                var sum = {}
                
                $.each (grid.columns, function () {
                    if (!this.is_to_sum) return
                    var k = this.field
                    sum [k] = 0.0
                })
                
                $.each (grid.records, function () {

                    if (this.w2ui) {
                    
                        var chg = this.w2ui.changes
                        
                        if (chg) for (var field in chg) {
                            var col = grid.getColumn (field)
                            this [field] = chg [field]
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
                                'background-color': '#eee',
                                'border-bottom-width': '1px',
                            }).css ({'border-right-width': 0})
                            
                            if (last) {
                            
                                sel = 'tr[recid=' + last + ']'

                                $(sel + ' td.w2ui-grid-data').css ({
                                    'border-bottom-width': '1px',
                                })
                            
                            }
                        
                        }
                        else if (this.id_type < 0) {

                            $(sel + ' td.w2ui-grid-data').css ({
                                'font-weight': 'bold',
                                'background-color': '#eee',
                                'border-bottom-color': '#aaa',
                            })

                        }
                        else {
                        
                            if (is_editing) {
                        
                                $(sel + ' td.w2ui-grid-data').each (function () {

                                    var $this = $(this)
                                    var col = grid.columns [$this.attr ('col')]

                                    if (is_yellow (row, col)) $this.css ({background: '#ffffcc'})

                                })

                            }
                            else {
                            
                                $(sel + ' td.w2ui-grid-data').each (function () {

                                    var $this = $(this)
                                    var col = grid.columns [$this.attr ('col')]
                                    
                                    var m = get_message (row, col)

                                    if (m) {
                                        $this.css ({background: '#ffffcc'}).attr ({title: m})
                                        $('div', $this).attr ({title: m})
                                    }
                                    else if (is_yellow (row, col) && null == row [col.field]) {
                                        $this.css ({background: '#ffffcc'})
                                    }
                                })                                

                            }

                        }

                        last = this.id                        

                    })

                }) 

            }

        }).refresh ()

    }
    
})