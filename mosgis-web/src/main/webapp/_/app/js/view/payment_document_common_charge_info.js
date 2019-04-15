define ([], function () {
    
    var grid_name = 'payment_document_common_charge_info_grid'
    
    function splash_edit () {
    
        alert ('Чтобы менять содержимое строк начисления, следует перейти в режим правки (кнопка "Редактировать")')

        var b = $('#tb_payment_document_common_charge_info_grid_toolbar_item_edit')
                
        function colorize (now) {
            b.css ('background-color', 'rgba(255,255,0,' + 0.8 * now + ')')
        }        
        
        b.animate ({aaa: 1}, {step: colorize}, 1000)
        b.animate ({aaa: 0}, {step: colorize}, 1000)
                
    }
    
    function get_message (row, col) {
    
        switch (col.field) {

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
                                
            default:
                return null

        }        
    
    }
    
    function is_yellow (row, col) {

        switch (col.field) {

            case 'rate':
                return !row.uuid_m_m_service
                
            case 'accountingperiodtotal':
                return true
                
            case 'calcexplanation':
                return row.uuid_gen_need_res

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
                grid.selectNone ()
                
                w2ui ['payment_document_common_form'].lock ()
                
                var tabs = w2ui ['passport_layout'].get ('main').tabs
                $.each (tabs.tabs, function () {
                    var id = this.id
                    if (id == 'payment_document_common_charge_info') return
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
        
        var m2 = {id: '055'}; m2.text = data.vc_okei [m2.id]

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

                {field: 'uuid_ins_product', caption: 'Наименование услуги', size: 50, editable: {type: 'list', items: data.tb_ins_products.items}, render: function (r, y, z, v) {return r.id_type == 50 ? data.tb_ins_products [v] : r.label}},
                
                {field: 'totalpayable', caption: 'Итого к оплате за расчетный период', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, is_to_sum: 1},
                {field: 'accountingperiodtotal', caption: 'Всего начислено за расчетный период (без перерасчетов и льгот)', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, is_to_sum: 1},

                {field: 'rate', caption: 'Тариф', size: 10, editable: {type: 'float', precision: 6, autoFormat: true, min: 0}},

                {field: 'cons_i_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true}, render: function (r, y, z, v) {return r.uuid_m_m_service ? w2utils.formatNumber (v) : '-'}},
                {field: 'cons_i_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'cons_o_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true}},
                {field: 'cons_o_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'okei', caption: 'Ед. изм.', size: 10, editable: function (r) {
                
                    var okei_orig = r.okei_orig; if (!okei_orig) return null
                    
                    var text  = data.vc_okei [okei_orig]                    
                    var items = [{id: okei_orig, text: text}]
                    
                    if (okei_orig != m2.id) {
                        if (text < m2.text) 
                            items.unshift (m2)
                        else
                            items.push (m2)                            
                    }
                    
                    return {type: 'list', items: items}
                    
                }, voc: data.vc_okei},

                {field: 'ratio', caption: 'Коэффициент', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}},
                {field: 'amountofexcessfees', caption: 'Размер превышения платы', size: 10, editable: {type: 'float', precision: 2, autoFormat: true}},

                {field: 'moneyrecalculation', caption: 'Сумма, руб', size: 10, editable: {type: 'float', precision: 2, autoFormat: true}, is_to_sum: 1},
                {field: 'recalculationreason', caption: 'Основание', size: 10, editable: {type: 'text'}},
                
                {field: 'moneydiscount', caption: 'Субсидии, скидки', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}, is_to_sum: 1},

                {field: 'calcexplanation', caption: 'Порядок расчётов', size: 10, editable: {type: 'text'}},
                
                {field: 'uuid_bnk_acct', caption: 'Пл. рекв.', size: 10, editable: {type: 'list'}, voc: data.tb_bnk_accts},

            ],

            records: data.lines,

            onDblClick: null,

            onChange: $_DO.patch_payment_document_common_charge_info,

            onEditField: function (e) {

                if (!is_editing) {
                    if (it._can.edit) splash_edit ()
                    return e.preventDefault ()
                }

                var grid = this

                var r = grid.get (e.recid)

                if (!r.id_type || r.id_type < 0) return e.preventDefault ()

                if (e.column == 0 && r.id_type != 50) return e.preventDefault ()

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
                            var editable = col.editable
                            if (typeof editable === "function") editable = editable (this)
                            if (!editable || editable.type != 'list') continue
                            this [field] = chg [field].uuid || chg [field].id
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

                if (is_editing) {                
                    var old = w2ui ['payment_document_common_form'].record.totalpayablebypd
                    old = parseFloat (old)
                    if (isNaN (old)) old = 0.0
                    var young = sum.totalpayable
darn ([old, young])
                    if (Math.abs (young - old) >= 0.01) $_DO.update_payment_document_common_charge_info (young)
                }

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
                                        $this.css ({background: '#ffcccc'})
                                        $('div', $this).attr ({title: 'Обязательно для заполнения'})
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