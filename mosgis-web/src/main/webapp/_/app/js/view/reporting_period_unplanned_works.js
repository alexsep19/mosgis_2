define ([], function () {

    return function (data, view) {
    
        var is_own = data.item._can.edit
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'reporting_period_unplanned_works_grid',

            show: {
                toolbar: is_own,
                toolbarReload: false,
                toolbarInput: true,
                toolbarAdd: true,
                toolbarEdit: true,
                toolbarDelete: true,
                footer: true,
            },     
            
            multiSelect: false,
            
            
            columns: [  
            
                {field: 'vc_nsi_56', caption: 'Вид работ/услуг', size: 30},
                {field: 'w.label', caption: 'Наименование', size: 50},
                {field: 'price', caption: 'Цена', size: 10, editable: !is_own ? null : {type: 'float:4', min: 0}},
                {field: 'count', caption: 'Количество', size: 10, editable: !is_own ? null : {type: 'int', min: 1}},
                {field: 'amount', caption: 'Объём', size: 10, editable: !is_own ? null : {type: 'float:3', min: 0}},
                {field: 'ok.national', caption: 'ед.', size: 5},
                {field: 'totalcost', caption: 'Общая стоимость', size: 20},
                {field: 'comment_', caption: 'Комментарий', size: 20},

            ],
            
            postData: {data: {
                uuid_reporting_period: $_REQUEST.id
            }},

            url: '/mosgis/_rest/?type=unplanned_works',

            onAdd: $_DO.create_reporting_period_unplanned_works,
            onEdit: $_DO.edit_reporting_period_unplanned_works,            
            onDblClick: $_DO.edit_reporting_period_unplanned_works,            
            onDelete: $_DO.delete_reporting_period_unplanned_works,

        }).refresh ();

    }

})