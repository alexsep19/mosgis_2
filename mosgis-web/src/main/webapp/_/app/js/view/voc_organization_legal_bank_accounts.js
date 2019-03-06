define ([], function () {

    var grid_name = 'voc_organization_legal_bank_accounts_grid'

    return function (data, view) {

        data._can = {
            edit: $_USER.role.admin || $_USER.uuid_org == $_REQUEST.id
        }

        var layout = w2ui ['voc_organization_legal_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({

            name: grid_name,

            show: {
                toolbar: data._can.edit,
                toolbarAdd: data._can.edit,
                footer: true,
                toolbarInput: false,
                toolbarReload: false,
                toolbarColumns: false,
            },

            textSearch: 'contains',

            columns: [
                {field: 'accountnumber', caption: '№ счёта', size: 15},
            ],

            records: data.records,

            onDblClick: null,
            
            onAdd: $_DO.create_voc_organization_legal_bank_accounts,

        }).refresh ()

    }

})