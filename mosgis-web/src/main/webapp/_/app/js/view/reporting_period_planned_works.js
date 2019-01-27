define ([], function () {

    return function (data, view) {
    
        var is_own = data.item._can.edit
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'reporting_period_planned_works_grid',

            show: {
                toolbar: is_own,
                toolbarReload: false,
                toolbarInput: false,
                footer: true,
            },     
            
            multiSelect: false,
            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'fill', caption: 'Заполнить плановыми показателями', onClick: $_DO.fill_reporting_period_planned_works, off: !is_own},
                ].filter (not_off),
                
            },

            columnGroups : [            
                {span: 3, caption: ' '},
                {span: 5, caption: 'Фактические показатели'},
                {span: 5, caption: 'Плановые показатели'},                
            ],
            
            columns: [  
            
                {field: 'li.index_', caption: '№', size: 1, editable: !is_own ? null : {type: 'int', min: 1}},
                {field: 'vc_nsi_56', caption: 'Вид работ/услуг', size: 30},
                {field: 'w.label', caption: 'Наименование', size: 50},
                
                {field: 'price', caption: 'Цена', size: 10, editable: !is_own ? null : {type: 'float:4', min: 0}},
                {field: 'amount', caption: 'Объём', size: 10, editable: !is_own ? null : {type: 'float:3', min: 0}},
                {field: 'count', caption: 'Количество', size: 10, editable: !is_own ? null : {type: 'int', min: 1}},
                {field: 'ok.national', caption: 'ед.', size: 5},
                {field: 'totalcost', caption: 'Стоимость', size: 20},
//                {field: '_', caption: 'Фото', size: 20},
                
                {field: 'li.price', caption: 'Цена', size: 10, render: 'float:2'},
                {field: 'li.amount', caption: 'Объём', size: 10},
                {field: 'workcount', caption: 'Количество по плану', size: 10},
                {field: 'ok.national', caption: 'ед.', size: 5},
                {field: '_plan_cost', caption: 'Общая стоимость', size: 20, render: 'float:2'},
                
            ],
            
            records: data.records,
                        
            onChange: $_DO.patch_reporting_period_planned_works,

        }).refresh ();

    }

})