/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
define ([], function () {

    return function (data, view) {

        var topmost_layout = w2ui ['topmost_layout']

        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'administr_layout',
            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'voc_users', caption: 'Учетные записи', off: !$_USER.role.admin},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_administr

                    }

                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

    }

});

