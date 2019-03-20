define ([], function () {

    return function (data, view) {
    
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarAdd: $_USER.role.admin,
                toolbarEdit: $_USER.role.admin,
                toolbarDelete: $_USER.role.admin,
                footer: true,
            },     
            
            searches: [            
                {field: 'is_locked', caption: 'Блокировка', type: 'enum', options: {items: [
                    {id: "0", text: "Активные"},
                    {id: "1", text: "Заблокированные"},
                ]}},
            ].filter (not_off),    

            columns: [                
                {field: 'label', caption: 'ФИО',    size: 100},
                {field: 'login', caption: 'login',  size: 100},
                {field: 'lockreason', caption: 'Причина запрета',  size: 100, hidden: true},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voc_users',
            
            onAdd:      $_DO.create_voc_organization_legal_users,
            
            onEdit:     $_DO.edit_voc_organization_legal_users,            
            onDblClick: !$_USER.role.admin ? null : $_DO.edit_voc_organization_legal_users,

            onDelete:   $_DO.delete_voc_organization_legal_users,
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            onColumnOnOff: function (e) {e.done (color_data_mandatory)},
            
        }).refresh ();

    }


})