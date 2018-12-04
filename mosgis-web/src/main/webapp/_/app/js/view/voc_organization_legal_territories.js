define ([], function () {

    return function (data, view) {

        var layout = w2ui ['voc_organization_legal_layout']

        var $panel = $(layout.el ('main'))
        
        $panel.w2regrid ({

            multiSelect: false,

            show: {
                toolbar: true,
                footer: true,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: false,
                toolbarAdd: $_USER.role.admin,
                toolbarDelete: $_USER.role.admin,
            },      

            name: 'voc_organization_legal_territories_grid',

            columns: [
                {field: 'code', caption: 'Код ОКТМО', size: 7},
                {field: 'label', caption: 'Наименование территории', size: 50},
            ],
            
            postData: {data: {"uuid_org": $_REQUEST.id}},
            url: '/mosgis/_rest/?type=voc_organization_territories',

            onAdd: $_DO.create_voc_organization_legal_territories,
            onDelete: $_DO.delete_voc_organization_legal_territories,

        })

    }

})