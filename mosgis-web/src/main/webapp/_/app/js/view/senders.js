define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarSearch: false,
                toolbarInput: false,
                footer: true,
            },     
/*
            searches: [            
                {field: 'label_uc',  caption: 'ФИО',  type: 'text'},
                {field: 'login_uc',  caption: 'Login',  type: 'text'},
                {field: 'code_vc_nsi_20', caption: 'Полномочия', type: 'enum', options: {items: data.vc_nsi_20.items}},
                {field: 'uuid_org', caption: 'Организация', type: 'list', options: {items: data.vc_orgs.items}},
            ],
            
Наименование системы
КСокращенное наименование системы
Ответственный за интеграцию
Интеграция отключена
GUID внешней системы            
            
*/
            columns: [                
                {field: 'uuid', caption: 'GUID',    size: 36},
                {field: 'label', caption: 'Сокр. наим.',    size: 20},
                {field: 'label_full', caption: 'Наименование системы',    size: 50},
                {field: 'contact', caption: 'Ответственный за интеграцию',    size: 50},
                {field: 'is_locked', caption: 'Интеграция отключена',  size: 10, voc: {1: 'отключена', 0: 'включена'}},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=senders',
            
            onAdd:      $_DO.create_senders,
            
            onEdit:     $_DO.edit_senders,            
            onDblClick: function (e) {
                
                if (e.column == 2) {
                    openTab ('/voc_organization_legal/' + this.get (e.recid).uuid_org)
                }
                else {
                    $_DO.edit_senders (e)
                }

            },

            onDelete:   $_DO.delete_senders,

        }).refresh ();

    }

})