define ([], function () {

    return function (data, view) {
    
        var is_own = data.item._can.edit
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_list_common_items_grid',

            show: {
                toolbar: is_own,
                toolbarReload: false,
                toolbarAdd: true,
                toolbarDelete: true,
                toolbarInput: false,
                footer: true,
            },     
            
            multiSelect: false,
            
            columns: [  
            
                {field: 'index_', caption: '№', size: 1, editable: !is_own ? null : {type: 'int', min: 1}},
                {field: 'vc_nsi_56', caption: 'Вид работ/услуг', size: 30},
                {field: 'w.label', caption: 'Наименование', size: 50},
                {field: 'price', caption: 'Цена', size: 10, editable: !is_own ? null : {type: 'float:4', min: 0}},
                {field: 'count', caption: 'Количество', size: 10, editable: !is_own ? null : {type: 'int', min: 1}},
                {field: 'amount', caption: 'Объём', size: 10, editable: !is_own ? null : {type: 'float:3', min: 0}},
                {field: 'ok.national', caption: 'ед.', size: 5},
                {field: 'totalcost', caption: 'Общая стоимость', size: 20},
                
            ],
            
            records: data.records,
            
            onAdd: function () {
                use.block ('working_list_common_items_popup')
            },
            
            onDelete: $_DO.delete_working_list_common_items,
            
            onChange: $_DO.patch_working_list_common_items,

        }).refresh ();

    }

})