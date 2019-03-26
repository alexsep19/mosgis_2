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

            textSearch: 'contains',

            columns: [                
                {field: 'label',        caption: 'Наименование услуги', size: 100},
                {field: 'totalpayable', caption: 'Итого к оплате за расчетный период', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false, min: 0}},
                {field: 'accountingperiodtotal', caption: 'Всего начислено за расчетный период (без перерасчетов и льгот)', size: 10, editable: {type: 'float', precision: 2, autoFormat: true, keyboard: false, min: 0}},

                {field: 'rate', caption: 'Тариф', size: 10, editable: {type: 'float', precision: 6, autoFormat: true, keyboard: false, min: 0}},

                {field: 'unit', caption: 'Ед. изм.', size: 10},                
                {field: 'calcexplanation', caption: 'Порядок расчётов', size: 10, editable: {type: 'text'}},

            ],

            records: [],
            
            onDblClick: null,
            
            onChange: $_DO.patch_payment_document_common_charge_info,
            
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