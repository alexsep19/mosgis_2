define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarEdit: true,
                toolbarDelete: true,
                footer: true,
            },               

            searches: [            
                {field: 'label_uc',  caption: 'ФИО',  type: 'text'},
                {field: 'login_uc',  caption: 'Login',  type: 'text'},
                {field: 'code_vc_nsi_20', caption: 'Полномочия', type: 'enum', options: {items: data.vc_nsi_20.items}},
                {field: 'uuid_org', caption: 'Организация', type: 'list', options: {items: data.vc_orgs.items}},
            ],

            columns: [                
                {field: 'label', caption: 'ФИО',    size: 30},
                {field: 'login', caption: 'login',  size: 20},
                {field: 'vc_orgs.label', caption: 'Огранизация',  size: 100},
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

        }).refresh ();

    }

})