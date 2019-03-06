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
                toolbarEdit: data._can.edit,
                footer: true,
                toolbarInput: false,
                toolbarReload: false,
                toolbarColumns: false,
            },

            textSearch: 'contains',

            columns: [
                {field: 'accountnumber', caption: 'Л/сч. №', size: 20},
                {field: 'bank.namep', caption: 'Банк', size: 30},
                {field: 'bank.bic', caption: 'БИК', size: 10},
                {field: 'bank.account', caption: 'Кор. сч. №', size: 10},
                {field: 'org.label', caption: 'Владелец счёта', size: 15},
                {field: 'opendate', caption: 'Открыт', size: 15, render: _dt},
                {field: 'closedate', caption: 'Закрыт', size: 15, render: _dt},
            ],

            url: '/mosgis/_rest/?type=bank_accounts',
            
            postData: {
                data: {uuid_org: $_REQUEST.id}
            },

            onDblClick: $_DO.edit_voc_organization_legal_bank_accounts,
            
            onAdd: $_DO.create_voc_organization_legal_bank_accounts,

            onEdit: $_DO.edit_voc_organization_legal_bank_accounts,

        }).refresh ()

    }

})