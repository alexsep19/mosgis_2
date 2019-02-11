define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        var it = data.item

        var can_edit = it._can.edit

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'account_common_individual_services_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: can_edit,
                toolbarEdit: can_edit,
                toolbarDelete: can_edit,
                footer: true,
            },     
            
            columns: [                
                {field: 'svc.label', caption: 'Услуга', size: 100},
                {field: 'begindate', caption: 'Начало', size: 10, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 10, render: _dt},
            ],

            postData: {data: {uuid_account: $_REQUEST.id}},
            
            url: '/mosgis/_rest/?type=account_individual_services',
            
            onAdd: $_DO.create_account_common_individual_services,
            onEdit: $_DO.edit_account_common_individual_services,
            onDblClick: $_DO.edit_account_common_individual_services,
            onDelete: $_DO.delete_account_common_individual_services,

        }).refresh ();

    }

})