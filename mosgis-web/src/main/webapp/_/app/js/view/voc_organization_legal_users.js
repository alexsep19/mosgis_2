define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarEdit: true,
                toolbarDelete: true,
                footer: true,
            },     
/*            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'import', caption: 'Импорт из ГИС ЖКХ...', onClick: $_DO.import_voc_organizations},
                ],
                
            }, 

            searches: [            
                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                {field: 'label_uc',  caption: 'Наименование / ФИО',  type: 'text'},
                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'code_vc_nsi_20', caption: 'Полномочия',     type: 'enum', options: {items: data.vc_nsi_20.items}},
                {field: 'id_type', caption: 'Типы',     type: 'enum', options: {items: data.vc_organization_types.items}},
            ],

*/            

            columns: [                
                {field: 'label', caption: 'ФИО',    size: 100},
                {field: 'login', caption: 'login',  size: 100},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voc_users',
            
            onAdd:      $_DO.create_voc_organization_legal_users,
            
            onEdit:     $_DO.edit_voc_organization_legal_users,            
            onDblClick: $_DO.edit_voc_organization_legal_users,

            onDelete:   $_DO.delete_voc_organization_legal_users,

        }).refresh ();

    }

})