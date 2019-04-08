define ([], function () {

    return function (data, view) {

        var is_popup = 1 == $_SESSION.delete ('houses_popup.on')
        
        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el ('main')).w2regrid ({

            name: 'houses_grid' + (is_popup ? '_popup' : ''),

            show: {
                toolbar: true,
                footer: true,
                toolbarInput: !is_popup,
            },

            toolbar: {
            
                items: [
                
                    {
                        type: 'button', 
                        id: 'import_objects', 
                        caption: 'Импорт паспорта ЖД', 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.import_houses, 
                        off: is_popup || (!$_USER.role.nsi_20_1 && !$_USER.role.admin)
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

            url: '/_back/?type=houses',

            onRequest: function (e) {

                var init_post_data = this.post_data

                if (is_popup) {

                    var post_data = this.post_data || $_SESSION.get('houses_popup.post_data');

                    if (post_data) {

                        if (e.postData.search) {
                            $.each(e.postData.search, function () {
                                post_data.search.push(this)
                            })
                        }

                        $.extend(e.postData, post_data)

                        this.post_data = post_data
                    }
                }

                e.done (function () {
                    this.post_data = init_post_data
                })
            },
            
            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {
                    $_SESSION.set ('houses_popup.data', clone (r))
                    w2popup.close ()
                }
                else {
                    openTab ('/house/' + e.recid)
                }
            }

        }).refresh ();

        $('#grid_houses_grid_search_all').focus ()

    }

})