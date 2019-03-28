define ([], function () {
    
    var grid_name = 'payment_document_common_charge_info_grid'
                
    return function (data, view) {

        $_F5 = function () {

            var grid = w2ui [grid_name]

            grid.records = data.lines
            
            $.each (grid.records, function () {
//                if (this.w2ui) delete this.w2ui.changes
//                delete this.value
            })

            grid.refresh ()

        }

        var layout = w2ui ['passport_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 

            name: grid_name,
            
            selectType: 'cell',

            show: {
                toolbar: 0,
                footer: 1,
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
                {span: 2, caption: 'Корректировки, руб'},
                
                {master: true},
                {master: true},

            ], 

            columns: [                

                {field: 'uuid_ins_product', caption: 'Наименование услуги', size: 50, editable: {type: 'list', items: data.tb_ins_products.items}, render: function (r, y, z, v) {return r.id_type == 50 ? data.tb_ins_products [v] : r.label}},
                
                {field: 'totalpayable', caption: 'Итого к оплате за расчетный период', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false, min: 0}},
                {field: 'accountingperiodtotal', caption: 'Всего начислено за расчетный период (без перерасчетов и льгот)', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false, min: 0}},

                {field: 'rate', caption: 'Тариф', size: 10, editable: {type: 'float', precision: 6, autoFormat: true, keyboard: false, min: 0}},

                {field: 'cons_i_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, keyboard: false, min: 0}},
                {field: 'cons_i_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'cons_o_vol', caption: 'Объём', size: 10, editable: {type: 'float', precision: 7, autoFormat: true, keyboard: false, min: 0}},
                {field: 'cons_o_dtrm_meth', caption: 'Определён по', size: 10, editable: {type: 'list'}, voc: data.vc_cnsmp_vol_dtrm},

                {field: 'unit', caption: 'Ед. изм.', size: 10},                

                {field: 'ratio', caption: 'Коэффициент', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false, min: 0}},
                {field: 'amountofexcessfees', caption: 'Размер превышения платы', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false}},

                {field: 'moneyrecalculation', caption: 'Перерасчет', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false}},
                {field: 'moneydiscount', caption: 'Субсидии, скидки', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false}},

                {field: 'calcexplanation', caption: 'Порядок расчётов', size: 10, editable: {type: 'text'}},
                
                {field: 'uuid_bnk_acct', caption: 'Пл. рекв.', size: 10, editable: {type: 'list'}, voc: data.tb_bnk_accts},

            ],

            records: [],
            
            onDblClick: null,
            
            onChange: $_DO.patch_payment_document_common_charge_info,
            
            onEditField: function (e) {

                var grid = this
                
                var r = grid.get (e.recid)
                
                if (!r.id_type) return e.preventDefault ()
                
                if (e.column == 0 && r.id_type != 50) return e.preventDefault ()

            },            
            
            onRefresh: function (e) {
            
                e.done (function () {
                    
                    var last = null

                    $.each (data.lines, function () {

                        if (!('id_type' in this)) {

                            var sel = 'tr[recid=' + this.recid + ']'

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
                        
                        last = this.id                        

                    })

                }) 
            
            }

        })

        $_F5 ()

    }
    
})