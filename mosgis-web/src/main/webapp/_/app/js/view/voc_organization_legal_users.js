define ([], function () {

    var b = ['lock', 'unlock']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['voc_organizations_grid']

        var t = g.toolbar

        t.disable (b[0]);
        t.disable (b[1]);
                
        if (g.getSelection ().length != 1) return
        t.enable (b [g.get (g.getSelection () [0]).is_locked])

    })}

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
            
            toolbar: {
                items: [
                    {type: 'button', id: 'lock', caption: 'Заблокировать', onClick: $_DO.lock_voc_user, disabled: true, off: !$_USER.role.admin},
                    {type: 'button', id: 'unlock', caption: 'Разблокировать', onClick: $_DO.unlock_voc_user, disabled: true, off: !$_USER.role.admin},
                ].filter (not_off),
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
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,
            
        }).refresh ();

    }


})