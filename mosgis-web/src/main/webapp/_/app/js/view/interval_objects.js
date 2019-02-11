define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        var it = data.item

        var can_edit = it._can.edit

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            name: 'interval_objects_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: can_edit,
                toolbarEdit: false,
                toolbarDelete: can_edit,
                footer: true,
            },     
            
            columns: [                
                {field: 'addr.label', caption: 'Адрес', size: 100},
                {field: 'prem.label', caption: '№ помещения', size: 10}
            ],

            postData: {data: {uuid_interval: $_REQUEST.id}},
            
            url: '/mosgis/_rest/?type=interval_objects',
            
            onAdd: $_DO.create_interval_objects,
            onEdit: $_DO.edit_interval_objects,
            onDblClick: $_DO.edit_interval_objects,
            onDelete: $_DO.delete_interval_objects,

        }).refresh ();

    }

})