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
    
        data = $('body').data ('data')
        
        $(w2ui ['administr_layout'].el ('main')).w2regrid ({ 
//        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarEdit: true,
                toolbarDelete: true,
                footer: true,
            },               

            toolbar: {
                items: [
                    {type: 'button', id: 'lock', caption: 'Заблокировать', onClick: $_DO.lock_voc_user, disabled: true, off: !$_USER.role.admin},
                    {type: 'button', id: 'unlock', caption: 'Разблокировать', onClick: $_DO.unlock_voc_user, disabled: true, off: !$_USER.role.admin},
                ].filter (not_off),
            }, 

            searches: [            
                {field: 'label_uc',  caption: 'ФИО',  type: 'text'},
                {field: 'login_uc',  caption: 'Login',  type: 'text'},
                {field: 'code_vc_nsi_20', caption: 'Полномочия', type: 'enum', options: {items: data.vc_nsi_20.items}},
                {field: 'uuid_org', caption: 'Организация', type: 'list', options: {items: data.vc_orgs.items}},
                {field: 'is_locked', caption: 'Блокировка', type: 'enum', options: {items: [
                    {id: "0", text: "Активные"},
                    {id: "1", text: "Заблокированные"},
                ]}},
            ].filter (not_off),

            columns: [                
                {field: 'label', caption: 'ФИО',    size: 30},
                {field: 'login', caption: 'login',  size: 20},
                {field: 'vc_orgs.label', caption: 'Огранизация',  size: 100},
                {field: 'lockreason', caption: 'Причина запрета',  size: 100, hidden: true},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voc_users',
            
            onAdd:      $_DO.create_voc_users,
            
            onEdit:     $_DO.edit_voc_users,            
            onDblClick: function (e) {
                
                if (e.column == 2) {
                    openTab ('/voc_organization_legal/' + this.get (e.recid).uuid_org)
                }
                else {
                    $_DO.edit_voc_users (e)
                }

            },

            onDelete:   $_DO.delete_voc_users,
            
            onRefresh: function (e) {e.done (color_data_mandatory)},
            onColumnOnOff: function (e) {e.done (color_data_mandatory)},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})