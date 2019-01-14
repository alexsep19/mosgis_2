define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organizations_grid',

            show: {
                toolbar: true,
                toolbarInput: true,
                footer: true,
            },     

            columns: [                
                {field: 'uuid', caption: 'GUID',    size: 36},
                {field: 'label', caption: 'Сокр. наим.',    size: 20},
                {field: 'label_full', caption: 'Наименование системы',    size: 50},
                {field: 'contact', caption: 'Ответственный за интеграцию',    size: 50},
                {field: 'is_locked', caption: 'Интеграция отключена',  size: 10, voc: {1: 'отключена', 0: 'включена'}},
            ],
            
            postData: {data: {uuid_org: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voc_unom',
                        
//            onDblClick: function (e) {openTab ('/voc_unom/' + e.recid)},

        }).refresh ();

    }

})