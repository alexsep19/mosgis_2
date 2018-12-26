define ([], function () {

    return function (data, view) {
    
        var it = data.item
        
        $('title').text (it.label)
        
        fill (view, it, $('#body'))

        $('#container').w2relayout({

            name: 'license_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'license_common',   caption: 'Общие'},
                        ].filter(not_off),

                        onClick: $_DO.choose_tab_license

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'license.active_tab')
            },

        });
    }

})