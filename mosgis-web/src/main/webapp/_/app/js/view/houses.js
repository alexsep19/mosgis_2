define ([], function () {

    return function (data, view) {
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'houses_grid',             

            show: {
                toolbar: true,
                footer: true,
            },

            toolbar: {
            
                items: [
                
                    {
                        type: 'button', 
                        id: 'import_objects', 
                        caption: 'Импорт паспорта ЖД', 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.import_houses, 
                        off: !$_USER.role.nsi_20_1
                    },
                    
                ].filter (not_off),
                
            },     
            
            searches: [
                {field: 'is_condo',            caption: 'Тип',           type: 'list', options: {items: [
                    {id: "1", text: 'МКД'},
                    {id: "0", text: 'ЖД'},
                ]}},
                {field: 'building.oktmo', caption: 'ОКТМО', type: 'text', operators: ['is'], operator: 'is'},
                {field: 'code_vc_nsi_24', caption: 'Состояние', type: 'list', options: {items: data.vc_nsi_24.items}},
                {field: 'id_status', caption: 'ГИС ЖКХ', type: 'list', options: { items: data.vc_house_status.items}},
                {field: 'address_uc',           caption: 'Адрес',         type: 'text'},
                {field: 'fiashouseguid',           caption: 'GUID ФИАС',         type: 'text', operators: ['null', 'not null', 'is'], operator: 'is'},
            ],

            columns: [                
                {field: 'is_condo',   caption: 'Тип дома', size: 10, render: function (r) {return r.is_condo ? 'МКД' : 'ЖД'}},
//                {field: 'unom', caption: 'UNOM',    size: 8},
                {field: 'fiashouseguid', caption: 'GUID ФИАС',    size: 30},
                {field: 'address', caption: 'Адрес',    size: 50},
                {field: 'oktmo', caption: 'ОКТМО', size: 15},
                {field: 'id_status', caption: 'Статус', size: 10, voc: data.vc_house_status},
                {field: 'code_vc_nsi_24', caption: 'Состояние', size: 20, voc: data.vc_nsi_24}
            ],

            url: '/mosgis/_rest/?type=houses',
            
            onDblClick: function (e) {
                openTab ('/house/' + e.recid)
            }

        }).refresh ();

        $('#grid_houses_grid_search_all').focus ()

    }

})