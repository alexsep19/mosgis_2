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

            case 'accountingperiodtotal':
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
                {span: 2, caption: 'Текущие показания ПУ'},
                {span: 2, caption: 'Суммарный объём коммунальных услуг'},
                {span: 2, caption: 'Норматив потребления коммунальных услуг'},
            ], 

            columns: [                

                {field: 'label', caption: 'Наименование услуги', size: 50},
                
                {field: 'label', caption: 'Наименование услуги', size: 50},
                {field: 'label', caption: 'Наименование услуги', size: 50},

                {field: 'label', caption: 'Наименование услуги', size: 50},
                {field: 'label', caption: 'Наименование услуги', size: 50},

                {field: 'label', caption: 'Наименование услуги', size: 50},
                {field: 'label', caption: 'Наименование услуги', size: 50},

/*                


        SI_VAL_IND            (Type.NUMERIC, 22, 7, null,   "Текущие показания приборов учёта коммунальных услуг - индивидуальное потребление (individualConsumptionCurrentValue)"),
        SI_VAL_OVER           (Type.NUMERIC, 22, 7, null,   "Текущие показания приборов учёта коммунальных услуг - общедомовые нужды (houseOverallNeedsCurrentValue)"),

        SI_HT_IND             (Type.NUMERIC, 22, 7, null,   "Суммарный объём коммунальных услуг в доме - индивидуальное потребление (houseTotalIndividualConsumption)"),
        SI_HT_OVER            (Type.NUMERIC, 22, 7, null,   "Суммарный объём коммунальных услуг в доме - общедомовые нужды (houseTotalHouseOverallNeeds)"),

        SI_IND_NORM           (Type.NUMERIC, 22, 7, null,   "Норматив потребления коммунальных услуг - индивидуальное потребление (individualConsumptionNorm)"),
        SI_HO_NORM            (Type.NUMERIC, 22, 7, null,   "Норматив потребления коммунальных услуг - общедомовые нужды (houseOverallNeedsNorm)"),

                
                {field: 'totalpayable', caption: 'Итого к оплате за расчетный период', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}},
                {field: 'accountingperiodtotal', caption: 'Всего начислено за расчетный период (без перерасчетов и льгот)', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}},

                {field: 'rate', caption: 'Тариф', size: 10, editable: {type: 'float', precision: 6, autoFormat: true, min: 0}},

                {field: 'cons_i_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}},
                {field: 'cons_i_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'cons_o_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, min: 0}},
                {field: 'cons_o_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'unit', caption: 'Ед. изм.', size: 10},                

                {field: 'ratio', caption: 'Коэффициент', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, min: 0}},
                {field: 'amountofexcessfees', caption: 'Размер превышения платы', size: 10, editable: {type: 'float', precision: 2, autoFormat: true}},

                {field: 'moneyrecalculation', caption: 'Перерасчет', size: 10, editable: {type: 'float', precision: 2, autoFormat: true}},
                {field: 'moneydiscount', caption: 'Субсидии, скидки', size: 10, editable: {type: 'float', precision: 2, autoFormat: true}},

                {field: 'calcexplanation', caption: 'Порядок расчётов', size: 10, editable: {type: 'text'}},
                
                {field: 'uuid_bnk_acct', caption: 'Пл. рекв.', size: 10, editable: {type: 'list'}, voc: data.tb_bnk_accts},
*/
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