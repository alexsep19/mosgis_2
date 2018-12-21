define ([], function () {

    return function (data, view) {
    
        var it = data.item
        //todo       
        it.vc_licenses_types_label = it ['license.label']

        $('title').text (it.label)

        fill (view, it, $('body'))

        $('#main_container').w2relayout({

            name: 'licenses_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
//                            {id: 'license_main', caption: 'Юридическое лицо'},
//                            {id: 'license_info', caption: 'Информация'},
//                            {id: 'license_hours', caption: 'Режим работы'}
                        ].filter(not_off),

                        onClick: $_DO.choose_top_tab_license

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'license.active_tab')
            },

        });
    }

})