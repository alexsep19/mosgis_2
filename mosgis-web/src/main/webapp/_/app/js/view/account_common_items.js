define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        var it = data.item

        var can_edit = it._can.edit

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'account_common_items_grid',

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
                {field: 'addr.label', caption: 'Адрес', size: 100},
                {field: 'prem.label', caption: '№ помещения', size: 10},
                {field: 'prem.totalarea', caption: 'Общ. пл., м\xB2', size: 10, render: 'float:2'},
                {field: 'sharepercent', caption: 'Доля, %', size: 20, render: 'float:2'},
            ],

            postData: {data: {uuid_account: $_REQUEST.id}},
            
            url: '/mosgis/_rest/?type=account_items',
            
            onAdd: $_DO.create_account_common_items,
            onEdit: $_DO.edit_account_common_items,
            onDblClick: $_DO.edit_account_common_items,
            onDelete: $_DO.delete_account_common_items,

        }).refresh ();

    }

})