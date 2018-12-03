define ([], function () {

    return function (data, view) {
    
        var it = data.item

        it.vc_organization_types_label = it ['vc_organization_types.label']

        $('title').text (it.label)

        fill (view, it, $('body'))

        $('#main_container').w2relayout({

            name: 'voc_organization_legal_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'voc_organization_legal_main', caption: 'Юридическое лицо'},
                            {id: 'voc_organization_legal_info', caption: 'Информация'},
                            {id: 'voc_organization_legal_hours', caption: 'Режим работы'},
                            {id: 'voc_organization_territories', caption: 'Территории', off: data.vc_orgs_nsi_20[0].code != 8}
                        ].filter(not_off),

                        onClick: $_DO.choose_top_tab_voc_organization_legal

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'voc_organization_legal.active_tab')
            },

        });
    }

})